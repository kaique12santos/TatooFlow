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
public class CalculoDivisaoRequest {
    private Long agendamentoId;
    private BigDecimal valorTotal;
    private BigDecimal custoInsumos;
}

