package com.tattooflow.modules.cliente;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Resposta da busca de clientes e orçamentos por nome ou tipo de trabalho [F5-BACK-02, UC04.1].
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteBuscaResponse {

    private Long id;
    private String nome;
    private String telefone;
    private String email;
    private String instagram;
    private String historicoSaude;

    /**
     * Tipo de trabalho principal ou mais recente associado ao cliente.
     */
    private String tipoTrabalho;

    /**
     * Lista com todos os tipos/estilos de trabalho associados ao cliente.
     */
    private List<String> tiposTrabalho;

    /**
     * Orçamentos/pré-agendamentos vinculados.
     */
    private List<OrcamentoResumoDTO> orcamentos;

    /**
     * Sessões/agendamentos vinculados.
     */
    private List<SessaoResumoDTO> sessoes;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrcamentoResumoDTO {
        private Long id;
        private String ideiasDescricao;
        private String referencia;
        private String localCorpo;
        private String dataDesejada;
        private String statusTriagem;
        private LocalDateTime dataCriacao;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessaoResumoDTO {
        private Long id;
        private Long tatuadorId;
        private LocalDateTime dataHora;
        private Integer duracaoMinutos;
        private String descricaoSessao;
        private BigDecimal valorEstimado;
        private String status;
    }
}

