package com.tattooflow.integrations.instagram;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class InstagramClient {

    private final RestTemplate restTemplate;

    public InstagramClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void publicarFotoPortfolio(String urlImagem, String legenda) {
        // Chamada à API Graph do Instagram para publicação de trabalhos
    }
}
