package com.tattooflow.modules.financeiro;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DivisaoSessaoDTO {
    private Long agendamentoId;
    private BigDecimal valorTotal;
    private BigDecimal custoInsumos;
    private BigDecimal baseCalculo;
    private BigDecimal porcentagemEstudio;
    private BigDecimal valorEstudio;
    private BigDecimal porcentagemArtista;
    private BigDecimal valorArtista;
    private String regra;
}

