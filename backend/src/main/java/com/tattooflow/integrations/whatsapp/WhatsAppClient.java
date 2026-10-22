package com.tattooflow.integrations.whatsapp;

import org.springframework.stereotype.Component;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class WhatsAppClient {

    private final RestTemplate restTemplate;
    private final WhatsAppProperties properties;

    public WhatsAppClient(RestTemplate restTemplate, WhatsAppProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public void enviarMensagem(String telefone, String conteudo) {
        enviar(Map.of("messaging_product", "whatsapp", "to", telefone,
                "type", "text", "text", Map.of("body", conteudo)));
    }

    public void enviarTemplate(String telefone, String nome, String idioma, List<String> parametros) {
        List<Map<String, String>> valores = parametros.stream()
                .map(texto -> Map.of("type", "text", "text", texto)).toList();
        enviar(Map.of("messaging_product", "whatsapp", "to", telefone, "type", "template",
                "template", Map.of("name", nome, "language", Map.of("code", idioma),
                        "components", List.of(Map.of("type", "body", "parameters", valores)))));
    }

    private void enviar(Map<String, Object> payload) {
        if (!properties.isEnabled()) {
            throw new IllegalStateException("Integração WhatsApp desabilitada");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(properties.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        String url = "https://graph.facebook.com/" + properties.getGraphApiVersion()
                + "/" + properties.getPhoneNumberId() + "/messages";
        restTemplate.postForEntity(url, new HttpEntity<>(payload, headers), Void.class);
    }
}
