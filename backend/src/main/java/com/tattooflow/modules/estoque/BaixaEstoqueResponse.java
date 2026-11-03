package com.tattooflow.modules.estoque;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaixaEstoqueResponse {
    private ItemEstoque item;
    private Integer quantidadeBaixada;
    private Integer saldoAnterior;
    private Integer saldoAtual;
    private boolean atingiuEstoqueMinimo;
    private boolean alertaDisparado;
    private String mensagem;
    private AlertaEstoqueDTO alerta;
}

