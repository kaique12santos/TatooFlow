package com.tattooflow.modules.midia;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * DTO de resposta da geração de legenda automática (F4-BACK-02, UC03.1, RN03).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LegendaResponse {

    private String title;
    private String body;
    private List<String> hashtags;
    private String legendaCompleta;
    private String estilo;
    private String localizacao;
    private String whatsappLink;
    private boolean geradoPorIa;
    private boolean fallback;
    private Map<String, Object> client;
}

