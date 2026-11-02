package com.tattooflow.modules.relatorio;

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
public class RelatorioRepasseItemDTO {
    private Long repasseId;
    private Long agendamentoId;
    private Long tatuadorId;
    private String nomeTatuador;
    private LocalDateTime data;
    private BigDecimal valorTotal;
    private BigDecimal custoInsumos;
    private BigDecimal baseCalculo;
    private BigDecimal comissaoEstudio;
    private BigDecimal valorRepasseArtista;
    private String status;
}
