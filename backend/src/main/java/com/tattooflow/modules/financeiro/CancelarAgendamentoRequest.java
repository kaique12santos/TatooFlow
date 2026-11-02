package com.tattooflow.modules.financeiro;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelarAgendamentoRequest {
    private LocalDateTime dataCancelamento;
    private Boolean forcarReembolsoIntegral;
    private BigDecimal percentualDesconto;
    private String motivo;
}

