package com.tattooflow.integrations.whatsapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class WhatsAppClientTest {
    private MockRestServiceServer server;
    private WhatsAppClient client;
    private WhatsAppProperties properties;

    @BeforeEach
    void preparar() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        properties = new WhatsAppProperties();
        properties.setEnabled(true);
        properties.setAccessToken("token-teste");
        properties.setPhoneNumberId("123456");
        properties.setGraphApiVersion("v23.0");
        client = new WhatsAppClient(restTemplate, properties);
    }

    @Test
    void deveEnviarTemplateOficialComParametrosPosicionais() {
        server.expect(requestTo("https://graph.facebook.com/v23.0/123456/messages"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer token-teste"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                    {"messaging_product":"whatsapp","to":"5511999999999","type":"template",
                     "template":{"name":"cuidados_pre_sessao","language":{"code":"pt_BR"},
                       "components":[{"type":"body","parameters":[
                         {"type":"text","text":"Ana"},
                         {"type":"text","text":"23/10/2026"},
                         {"type":"text","text":"12:00"}]}]}}
                    """, true))
                .andRespond(withSuccess("{\"messages\":[{\"id\":\"wamid.teste\"}]}", MediaType.APPLICATION_JSON));
        client.enviarTemplate("5511999999999", "cuidados_pre_sessao", "pt_BR",
                List.of("Ana", "23/10/2026", "12:00"));
        server.verify();
    }

    @Test
    void deveEnviarTextoPelaMesmaInfraestrutura() {
        server.expect(requestTo("https://graph.facebook.com/v23.0/123456/messages"))
                .andExpect(jsonPath("$.type").value("text"))
                .andExpect(jsonPath("$.text.body").value("Olá, Ana!"))
                .andRespond(withSuccess());
        client.enviarMensagem("5511999999999", "Olá, Ana!");
        server.verify();
    }

    @Test
    void devePropagarFalhaDoProvedorParaPermitirNovaTentativa() {
        server.expect(requestTo("https://graph.facebook.com/v23.0/123456/messages"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        assertThatThrownBy(() -> client.enviarTemplate("5511999999999", "pre", "pt_BR", List.of("Ana")))
                .isInstanceOf(RestClientException.class);
        server.verify();
    }

    @Test
    void integracaoDesabilitadaNaoDeveChamarMeta() {
        properties.setEnabled(false);
        assertThatThrownBy(() -> client.enviarMensagem("5511999999999", "Olá"))
                .isInstanceOf(IllegalStateException.class);
        server.verify();
    }
}
