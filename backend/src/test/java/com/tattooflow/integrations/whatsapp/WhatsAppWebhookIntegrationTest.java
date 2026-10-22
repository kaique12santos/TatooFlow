package com.tattooflow.integrations.whatsapp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:whatsapp_webhook_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1",
        "integrations.whatsapp.enabled=true",
        "integrations.whatsapp.verify-token=test-verify-token",
        "integrations.whatsapp.app-secret=test-app-secret",
        "integrations.whatsapp.access-token=test-access-token",
        "integrations.whatsapp.phone-number-id=123456789",
        "integrations.whatsapp.business-account-id=987654321",
        "integrations.whatsapp.graph-api-version=v99.0"
})
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@RecordApplicationEvents
class WhatsAppWebhookIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationEvents events;

    // O listener de triagem responde ao webhook; o transporte externo deve ser isolado.
    @MockBean
    private WhatsAppClient whatsAppClient;

    @Test
    void deveDevolverChallengeExatoSemJwt() throws Exception {
        mockMvc.perform(get("/api/whatsapp/webhook")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "test-verify-token")
                        .param("hub.challenge", "00123456789"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("00123456789"));
    }

    @Test
    void deveDevolverChallengeExatoNaRotaSemPrefixoApi() throws Exception {
        mockMvc.perform(get("/whatsapp/webhook")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "test-verify-token")
                        .param("hub.challenge", "98765432100"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("98765432100"));
    }

    @Test
    void deveRejeitarTokenIncorreto() throws Exception {
        mockMvc.perform(get("/api/whatsapp/webhook")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "wrong-token")
                        .param("hub.challenge", "123"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRejeitarTokenAusente() throws Exception {
        mockMvc.perform(get("/api/whatsapp/webhook")
                        .param("hub.mode", "subscribe")
                        .param("hub.challenge", "123"))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "unsubscribe"})
    void deveRejeitarModoInvalido(String mode) throws Exception {
        mockMvc.perform(get("/api/whatsapp/webhook")
                        .param("hub.mode", mode)
                        .param("hub.verify_token", "test-verify-token")
                        .param("hub.challenge", "123"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRejeitarChallengeAusente() throws Exception {
        mockMvc.perform(get("/api/whatsapp/webhook")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "test-verify-token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveAceitarEventoDeTesteAssinadoSemJwt() throws Exception {
        byte[] payload = "{\"object\":\"whatsapp_business_account\",\"teste\":\"Olá 👋\"}"
                .getBytes(StandardCharsets.UTF_8);
        mockMvc.perform(post("/api/whatsapp/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Hub-Signature-256", sign(payload))
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deveReceberEParsarMensagemPublicandoEvento() throws Exception {
        String json = """
                {
                  "object": "whatsapp_business_account",
                  "entry": [
                    {
                      "id": "987654321",
                      "changes": [
                        {
                          "field": "messages",
                          "value": {
                            "messaging_product": "whatsapp",
                            "metadata": {
                              "display_phone_number": "5511000000000",
                              "phone_number_id": "123456789"
                            },
                            "contacts": [
                              {
                                "profile": { "name": "Carlos Eduardo" },
                                "wa_id": "5511999998888"
                              }
                            ],
                            "messages": [
                              {
                                "from": "5511999998888",
                                "id": "wamid.real.001",
                                "timestamp": "1729000500",
                                "type": "text",
                                "text": { "body": "Gostaria de agendar uma tatuagem oriental" }
                              }
                            ]
                          }
                        }
                      ]
                    }
                  ]
                }
                """;

        byte[] payload = json.getBytes(StandardCharsets.UTF_8);

        mockMvc.perform(post("/api/whatsapp/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Hub-Signature-256", sign(payload))
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        var eventosRecebidos = events.stream(WhatsAppMessageReceivedEvent.class).toList();
        assertThat(eventosRecebidos).isNotEmpty();
        var evento = eventosRecebidos.stream()
                .filter(e -> "wamid.real.001".equals(e.messageId()))
                .findFirst()
                .orElseThrow();

        assertThat(evento.source()).isEqualTo("META");
        assertThat(evento.telefone()).isEqualTo("5511999998888");
        assertThat(evento.nomeContato()).isEqualTo("Carlos Eduardo");
        assertThat(evento.texto()).isEqualTo("Gostaria de agendar uma tatuagem oriental");
        assertThat(evento.tipo()).isEqualTo("text");
    }

    @Test
    void deveReceberEParsarPayloadCompativelComMock() throws Exception {
        // Payload idêntico ao gerado pelo WhatsAppMockService da F2-INFRA-02
        String json = """
                {
                  "object": "whatsapp_business_account",
                  "entry": [
                    {
                      "id": "mock-waba-id",
                      "changes": [
                        {
                          "field": "messages",
                          "value": {
                            "messaging_product": "whatsapp",
                            "metadata": {
                              "display_phone_number": "5500000000000",
                              "phone_number_id": "mock-phone-number-id"
                            },
                            "contacts": [
                              {
                                "wa_id": "5511988887777",
                                "profile": { "name": "Cliente de teste" }
                              }
                            ],
                            "messages": [
                              {
                                "from": "5511988887777",
                                "id": "wamid.mock.test-123",
                                "timestamp": "1729000600",
                                "type": "text",
                                "text": { "body": "Teste mock payload via webhook" }
                              }
                            ]
                          }
                        }
                      ]
                    }
                  ]
                }
                """;

        byte[] payload = json.getBytes(StandardCharsets.UTF_8);

        mockMvc.perform(post("/whatsapp/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Hub-Signature-256", sign(payload))
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        var evento = events.stream(WhatsAppMessageReceivedEvent.class)
                .filter(e -> "wamid.mock.test-123".equals(e.messageId()))
                .findFirst()
                .orElseThrow();

        assertThat(evento.telefone()).isEqualTo("5511988887777");
        assertThat(evento.texto()).isEqualTo("Teste mock payload via webhook");
        assertThat(evento.nomeContato()).isEqualTo("Cliente de teste");
    }

    @Test
    void deveRejeitarPayloadAlteradoAposAssinatura() throws Exception {
        mockMvc.perform(post("/api/whatsapp/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Hub-Signature-256", sign("{\"entry\":[]}".getBytes(StandardCharsets.UTF_8)))
                        .content("{\"entry\":[{}]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRejeitarEventoSemAssinatura() throws Exception {
        mockMvc.perform(post("/api/whatsapp/webhook")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "sha256=invalid", "sha1=abc", "sha256=0000000000000000000000000000000000000000000000000000000000000000"})
    void deveRejeitarAssinaturaMalformadaOuIncorreta(String signature) throws Exception {
        mockMvc.perform(post("/api/whatsapp/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Hub-Signature-256", signature).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void demaisRotasDevemContinuarProtegidas() throws Exception {
        mockMvc.perform(get("/api/clientes")).andExpect(status().isForbidden());
    }

    private String sign(byte[] payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("test-app-secret".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return "sha256=" + HexFormat.of().formatHex(mac.doFinal(payload));
    }
}
