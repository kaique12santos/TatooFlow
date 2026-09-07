package com.tattooflow.integrations.gemini;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GeminiClient {

    private final RestTemplate restTemplate;

    public GeminiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String analisarIdeiaTatuagem(String descricao) {
        // Chamada à API da Gemini para assistência de IA na triagem/geração de ideias
        return "Sugestão da IA para: " + descricao;
    }
}
