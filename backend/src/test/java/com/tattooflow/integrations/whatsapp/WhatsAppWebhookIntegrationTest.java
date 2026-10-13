package com.tattooflow.integrations.whatsapp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class WhatsAppWebhookIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
