package com.tattooflow.integrations.whatsapp;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class WhatsAppClient {

    private final RestTemplate restTemplate;

    public WhatsAppClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void enviarMensagem(String telefone, String conteudo) {
        // Integração com WhatsApp Business API / Provider
    }
}
