package com.tattooflow.modules.postagem;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO de resposta do agendamento de postagem no Instagram [F4-BACK-03, UC03.2].
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostagemAgendadaResponse {

    private Long id;
    private String protocolo;

    @JsonProperty("protocol")
    public String getProtocol() {
        return protocolo;
    }

    private String imageUrl;
    private String legenda;
    private LocalDateTime dataHoraAgendada;
    private LocalDateTime dataHoraPublicacao;
    private String status;
    private String instagramContainerId;
    private String instagramMediaId;
    private String canais;
    private String contaInstagram;
    private String mensagemErro;
    private boolean success;
    private String message;

    public static PostagemAgendadaResponse fromEntity(PostagemAgendada entity) {
        if (entity == null) {
            return null;
        }
        return PostagemAgendadaResponse.builder()
                .id(entity.getId())
                .protocolo(entity.getProtocolo())
                .imageUrl(entity.getImageUrl())
                .legenda(entity.getLegenda())
                .dataHoraAgendada(entity.getDataHoraAgendada())
                .dataHoraPublicacao(entity.getDataHoraPublicacao())
                .status(entity.getStatus())
                .instagramContainerId(entity.getInstagramContainerId())
                .instagramMediaId(entity.getInstagramMediaId())
                .canais(entity.getCanais())
                .contaInstagram(entity.getContaInstagram())
                .mensagemErro(entity.getMensagemErro())
                .success("PUBLICADO".equalsIgnoreCase(entity.getStatus()) || "AGENDADO".equalsIgnoreCase(entity.getStatus()))
                .message("Postagem agendada com sucesso com protocolo " + entity.getProtocolo())
                .build();
    }
}

