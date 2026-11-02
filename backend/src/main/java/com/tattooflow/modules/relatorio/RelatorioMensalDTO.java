package com.tattooflow.modules.relatorio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioMensalDTO {

    private int mes;
    private int ano;
    private String periodo;
    private LocalDateTime periodoInicio;
    private LocalDateTime periodoFim;

    // Métricas principais consolidadas [F8-BACK-01]
    private BigDecimal totalFaturamento;
    private BigDecimal faturamentoBruto;
    private BigDecimal faturamentoRecebido;

    private BigDecimal totalComissoes;
    private BigDecimal comissaoEstudio;

    private BigDecimal totalRepasses;
    private BigDecimal totalRepasseArtistas;

    private BigDecimal totalGastos;
    private BigDecimal totalGastosInsumos;

    private BigDecimal lucroLiquido;
    private BigDecimal lucroLiquidoEstudio;

    private int totalSessoes;
    private int totalSessoesConcluidas;
    private int totalAgendamentosPeriodo;

    // Detalhamento por Artista/Tatuador
    private List<RelatorioArtistaDTO> detalhamentoArtistas;

    // Detalhamento de cada repasse do período
    private List<RelatorioRepasseItemDTO> detalhamentoRepasses;

    // Resumo consolidado de estoque de insumos
    private RelatorioEstoqueResumoDTO resumoEstoque;
}

