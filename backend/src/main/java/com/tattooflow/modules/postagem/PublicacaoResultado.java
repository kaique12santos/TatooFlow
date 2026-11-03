package com.tattooflow.modules.postagem;

import lombok.*;

/**
 * Resultado da execução da publicação via Instagram Graph API [F4-BACK-03].
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicacaoResultado {

    private boolean sucesso;
    private String containerId;
    private String mediaId;
    private String respostaJson;
    private String mensagemErro;

    public static PublicacaoResultado sucesso(String containerId, String mediaId, String respostaJson) {
        return PublicacaoResultado.builder()
                .sucesso(true)
                .containerId(containerId)
                .mediaId(mediaId)
                .respostaJson(respostaJson)
                .build();
    }

    public static PublicacaoResultado falha(String mensagemErro, String respostaJson) {
        return PublicacaoResultado.builder()
                .sucesso(false)
                .mensagemErro(mensagemErro)
                .respostaJson(respostaJson)
                .build();
    }
}

