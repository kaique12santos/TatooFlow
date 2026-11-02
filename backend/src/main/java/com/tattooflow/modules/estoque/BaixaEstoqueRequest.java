package com.tattooflow.modules.estoque;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaixaEstoqueRequest {
    private Integer quantidade;
    private String motivo;
    private Long agendamentoId;
}

