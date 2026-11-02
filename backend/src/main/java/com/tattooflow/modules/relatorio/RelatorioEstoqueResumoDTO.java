package com.tattooflow.modules.relatorio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioEstoqueResumoDTO {
    private long totalItensCadastrados;
    private long itensEmEstoqueCritico;
    private boolean alertaEstoqueAtivo;
}

