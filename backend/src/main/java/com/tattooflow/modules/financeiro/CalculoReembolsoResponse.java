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
public class CalculoReembolsoResponse {
    private Long agendamentoId;
    private LocalDateTime dataAgendamento;
    private LocalDateTime dataCancelamento;
    private Long horasAntecedencia;
    private boolean menosDe48Horas;
    private boolean reembolsoIntegralPermitido;
    private BigDecimal valorPago;
    private BigDecimal percentualDesconto;
    private BigDecimal valorDesconto;
    private BigDecimal valorReembolsavel;
    private String politica;
    private String mensagem;
}

