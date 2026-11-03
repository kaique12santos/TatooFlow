package com.tattooflow.integrations.whatsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:whatsapp_mock_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1",
        "integrations.whatsapp.enabled=false",
        "integrations.whatsapp.mock.enabled=true"
})
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@RecordApplicationEvents
class WhatsAppMockIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationEvents events;

    @MockBean
    private WhatsAppClient whatsAppClient;

    @AfterEach
    void naoDeveChamarClienteOficial() {
        verifyNoInteractions(whatsAppClient);
    }

    @Test
    void deveSimularMensagemSemMetaOuJwtEPublicarEventoLocal() throws Exception {
        String texto = "Quero uma tatuagem: \"flor\" 🌸\nno braço.";
        Instant inicio = Instant.now().minusSeconds(1);
        String response = mockMvc.perform(post("/api/whatsapp/mock/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("telefone", "5511999999999", "texto", texto))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.source").value("MOCK"))
                .andExpect(jsonPath("$.data.telefone").value("5511999999999"))
                .andExpect(jsonPath("$.data.texto").value(texto))
                .andExpect(jsonPath("$.data.payload.object").value("whatsapp_business_account"))
                .andExpect(jsonPath("$.data.payload.entry[0].changes[0].field").value("messages"))
                .andExpect(jsonPath("$.data.payload.entry[0].changes[0].value.messaging_product").value("whatsapp"))
                .andExpect(jsonPath("$.data.payload.entry[0].changes[0].value.messages[0].from").value("5511999999999"))
                .andExpect(jsonPath("$.data.payload.entry[0].changes[0].value.messages[0].type").value("text"))
                .andExpect(jsonPath("$.data.payload.entry[0].changes[0].value.messages[0].text.body").value(texto))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        var receivedEvents = events.stream(WhatsAppMockMessageReceivedEvent.class).toList();
        assertThat(receivedEvents).hasSize(1);
        var event = receivedEvents.get(0);
        var data = objectMapper.readTree(response).path("data");
        assertThat(event.messageId()).startsWith("wamid.mock.").isEqualTo(data.path("messageId").asText());
        assertThat(event.receivedAt()).isBetween(inicio, Instant.now());
        assertThat(event.payload()).isEqualTo(data.path("payload"));
        assertThat(event.payload().at("/entry/0/changes/0/value/messages/0/timestamp").asText())
                .isEqualTo(Long.toString(event.receivedAt().getEpochSecond()));
        assertThat(event.payload().at("/entry/0/changes/0/value/messages/0/id").asText())
                .isEqualTo(event.messageId());
    }

    @Test
    void deveGerarIdentificadorDistintoEmCadaRecebimento() throws Exception {
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/whatsapp/mock/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"telefone\":\"5511999999999\",\"texto\":\"Oi\"}"))
                    .andExpect(status().isOk());
        }
        assertThat(events.stream(WhatsAppMockMessageReceivedEvent.class)
                .map(WhatsAppMockMessageReceivedEvent::messageId).toList())
                .hasSize(2).doesNotHaveDuplicates();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"telefone\":null,\"texto\":\"Oi\"}",
            "{\"telefone\":\"5511999999999\",\"texto\":null}",
            "{\"telefone\":\"5511999999999\",\"texto\":\"   \"}",
            "{\"telefone\":\"+5511999999999\",\"texto\":\"Oi\"}",
            "{\"telefone\":\"abc\",\"texto\":\"Oi\"}",
            "{\"telefone\":\"123\",\"texto\":\"Oi\"}",
            "{\"telefone\":\"1234567890123456\",\"texto\":\"Oi\"}",
            "{\"telefone\":\"0511999999999\",\"texto\":\"Oi\"}",
            "{invalid"
    })
    void deveRejeitarEntradaInvalidaSemPublicarEvento(String body) throws Exception {
        mockMvc.perform(post("/api/whatsapp/mock/messages")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        assertThat(events.stream(WhatsAppMockMessageReceivedEvent.class)).isEmpty();
    }

    @Test
    void deveLimitarTamanhoDoTexto() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("telefone", "5511999999999", "texto", "a".repeat(4097)));
        mockMvc.perform(post("/api/whatsapp/mock/messages").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        assertThat(events.stream(WhatsAppMockMessageReceivedEvent.class)).isEmpty();
    }

    @Test
    void deveAceitarTextoNoLimite() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("telefone", "5511999999999", "texto", "a".repeat(4096)));
        mockMvc.perform(post("/api/whatsapp/mock/messages").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        assertThat(events.stream(WhatsAppMockMessageReceivedEvent.class).count()).isEqualTo(1);
    }

    @Test
    void deveExigirCorpoJson() throws Exception {
        mockMvc.perform(post("/api/whatsapp/mock/messages").contentType(MediaType.TEXT_PLAIN).content("Oi"))
                .andExpect(status().isUnsupportedMediaType());
        mockMvc.perform(post("/api/whatsapp/mock/messages").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        assertThat(events.stream(WhatsAppMockMessageReceivedEvent.class)).isEmpty();
    }

    @Test
    void mockNaoDeveHabilitarWebhookRealNemLiberarOutrasRotas() throws Exception {
        mockMvc.perform(post("/api/whatsapp/webhook").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isServiceUnavailable());
        mockMvc.perform(get("/api/clientes")).andExpect(status().isForbidden());
    }
}
