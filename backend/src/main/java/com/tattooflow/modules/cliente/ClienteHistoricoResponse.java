package com.tattooflow.modules.cliente;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Resposta do histórico completo do cliente [F5-BACK-03].
 * Contém sessões/agendamentos, termos de aceite assinados e mídias vinculadas.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteHistoricoResponse {

    private ClienteResumoDTO cliente;
    private List<SessaoHistoricoDTO> sessoes;
    private List<TermoAceiteDTO> termos;
    private List<MidiaDTO> midias;
    private Integer totalSessoes;
    private Integer totalTermos;
    private Integer totalMidias;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClienteResumoDTO {
        private Long id;
        private String nome;
        private String telefone;
        private String email;
        private String instagram;
        private String historicoSaude;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessaoHistoricoDTO {
        private Long id;
        private Long tatuadorId;
        private LocalDateTime dataHora;
        private Integer duracaoMinutos;
        private String descricaoSessao;
        private BigDecimal valorEstimado;
        private String status;
        private List<MidiaDTO> midias;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TermoAceiteDTO {
        private Long id;
        private Long agendamentoId;
        private String urlPdfAssinado;
        private LocalDateTime dataAssinatura;
        private String ipAssinatura;
        private LocalDateTime dataExpiracaoRetencao;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MidiaDTO {
        private Long id;
        private Long agendamentoId;
        private String url;
        private String tipo;
        private LocalDateTime dataUpload;
    }
}

