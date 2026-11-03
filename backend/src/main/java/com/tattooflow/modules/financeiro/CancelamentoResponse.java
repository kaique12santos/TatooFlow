package com.tattooflow.modules.financeiro;

import com.tattooflow.modules.agendamento.Agendamento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelamentoResponse {
    private Agendamento agendamento;
    private CalculoReembolsoResponse reembolso;
    private Pagamento pagamentoReembolso;
}

