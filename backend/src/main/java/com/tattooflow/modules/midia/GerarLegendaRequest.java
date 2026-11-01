package com.tattooflow.modules.midia;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Map;

/**
 * DTO para solicitação de geração de legenda de foto de tatuagem via Gemini API (F4-BACK-02, UC03.1, RN03).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GerarLegendaRequest {

    private Long midiaId;
    private Long agendamentoId;

    @JsonAlias({"image_url", "image", "masterShotUrl"})
    private String imageUrl;

    @JsonAlias({"artworkStyle", "style"})
    private String estilo;

    @JsonAlias({"location", "local", "station"})
    private String localizacao;

    @JsonAlias({"whatsapp_link", "whatsapp", "whatsappNumber"})
    private String whatsappLink;

    @JsonAlias({"tone"})
    private String tom; // chiaroscuro, solene, story

    @JsonAlias({"artistName", "artist"})
    private String artista;

    @JsonAlias({"clientName"})
    private String nomeCliente;

    @JsonAlias({"artworkTitle", "description", "detalhes", "sessionInfo"})
    private String descricao;

    /**
     * Suporte para o payload do frontend { tone, imageUrl, client: { ... } }
     */
    @JsonAlias({"client"})
    private Map<String, Object> client;
}

