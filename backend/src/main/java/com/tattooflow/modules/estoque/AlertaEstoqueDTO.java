package com.tattooflow.modules.estoque;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaEstoqueDTO {
    private Long itemId;
    private String nomeItem;
    private String categoria;
    private Integer quantidadeAtual;
    private Integer quantidadeMinima;
    private String unidadeMedida;
    private LocalDateTime dataAlerta;
    private String mensagem;
    private List<String> administradoresNotificados;
}

