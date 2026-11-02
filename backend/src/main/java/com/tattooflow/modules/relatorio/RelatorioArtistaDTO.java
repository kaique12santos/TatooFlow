package com.tattooflow.modules.relatorio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioArtistaDTO {
    private Long tatuadorId;
    private String nomeTatuador;
    private int totalSessoes;
    private BigDecimal totalFaturado;
    private BigDecimal baseCalculo;
    private BigDecimal comissaoEstudio;
    private BigDecimal repasseArtista;
    private BigDecimal custoInsumos;
}
