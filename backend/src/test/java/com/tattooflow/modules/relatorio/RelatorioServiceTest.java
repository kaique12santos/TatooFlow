package com.tattooflow.modules.relatorio;

import com.tattooflow.common.exception.BusinessException;
import com.tattooflow.modules.agendamento.Agendamento;
import com.tattooflow.modules.agendamento.AgendamentoRepository;
import com.tattooflow.modules.estoque.EstoqueRepository;
import com.tattooflow.modules.estoque.ItemEstoque;
import com.tattooflow.modules.financeiro.FinanceiroRepository;
import com.tattooflow.modules.financeiro.Pagamento;
import com.tattooflow.modules.financeiro.Repasse;
import com.tattooflow.modules.financeiro.RepasseRepository;
import com.tattooflow.modules.usuario.Usuario;
import com.tattooflow.modules.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock
    private RepasseRepository repasseRepository;

    @Mock
    private FinanceiroRepository financeiroRepository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private EstoqueRepository estoqueRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private RelatorioService relatorioService;

    @Test
    @DisplayName("[F8-BACK-01] Deve lançar exceção para mês menor que 1")
    void deveLancarExcecaoParaMesMenorQueUm() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                relatorioService.gerarRelatorioMensal(0, 2026));
        assertTrue(ex.getMessage().contains("Mês inválido"));
    }

    @Test
    @DisplayName("[F8-BACK-01] Deve lançar exceção para mês maior que 12")
    void deveLancarExcecaoParaMesMaiorQueDoze() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                relatorioService.gerarRelatorioMensal(13, 2026));
        assertTrue(ex.getMessage().contains("Mês inválido"));
    }

    @Test
    @DisplayName("[F8-BACK-01] Deve lançar exceção para ano inválido")
    void deveLancarExcecaoParaAnoInvalido() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                relatorioService.gerarRelatorioMensal(11, 1990));
        assertTrue(ex.getMessage().contains("Ano inválido"));
    }

    @Test
    @DisplayName("[F8-BACK-01] Deve gerar relatório zerado quando não houver movimentações no mês")
    void deveGerarRelatorioZeradoSemMovimentacoes() {
        when(repasseRepository.buscarPorPeriodo(any(), any())).thenReturn(Collections.emptyList());
        when(agendamentoRepository.listarPorPeriodoEStatus(any(), any(), any(), isNull())).thenReturn(Collections.emptyList());
        when(financeiroRepository.buscarPorPeriodo(any(), any())).thenReturn(Collections.emptyList());
        when(estoqueRepository.count()).thenReturn(5L);
        when(estoqueRepository.buscarItensEmEstoqueMinimo()).thenReturn(Collections.emptyList());

        RelatorioMensalDTO relatorio = relatorioService.gerarRelatorioMensal(11, 2026);

        assertNotNull(relatorio);
        assertEquals(11, relatorio.getMes());
        assertEquals(2026, relatorio.getAno());
        assertEquals("11/2026", relatorio.getPeriodo());
        assertEquals(new BigDecimal("0.00"), relatorio.getTotalFaturamento());
        assertEquals(new BigDecimal("0.00"), relatorio.getTotalComissoes());
        assertEquals(new BigDecimal("0.00"), relatorio.getTotalRepasses());
        assertEquals(new BigDecimal("0.00"), relatorio.getTotalGastos());
        assertEquals(new BigDecimal("0.00"), relatorio.getLucroLiquido());
        assertEquals(0, relatorio.getTotalSessoes());
        assertTrue(relatorio.getDetalhamentoArtistas().isEmpty());
        assertTrue(relatorio.getDetalhamentoRepasses().isEmpty());
        assertEquals(5, relatorio.getResumoEstoque().getTotalItensCadastrados());
        assertEquals(0, relatorio.getResumoEstoque().getItensEmEstoqueCritico());
        assertFalse(relatorio.getResumoEstoque().isAlertaEstoqueAtivo());
    }

    @Test
    @DisplayName("[F8-BACK-01] Deve consolidar faturamento, comissões (30%), repasses (70%) e gastos com insumos com dados de exemplo")
    void deveConsolidarFaturamentoComissoesEGastosComDadosDeExemplo() {
        LocalDateTime dataSessao = LocalDateTime.of(2026, 11, 15, 14, 0);

        // Agendamento 1: R$ 1000 total, R$ 100 insumos -> Base R$ 900 -> R$ 270 estúdio (30%) / R$ 630 artista 1 (70%)
        Agendamento ag1 = Agendamento.builder()
                .id(1L)
                .tatuadorId(10L)
                .valorEstimado(new BigDecimal("1000.00"))
                .status("CONCLUIDO")
                .dataHora(dataSessao)
                .build();

        Repasse rep1 = Repasse.builder()
                .id(101L)
                .agendamentoId(1L)
                .tatuadorId(10L)
                .custoInsumos(new BigDecimal("100.00"))
                .valorRepasse(new BigDecimal("630.00"))
                .valorLiquido(new BigDecimal("630.00"))
                .porcentagemEstudio(new BigDecimal("30.00"))
                .dataRepasse(dataSessao)
                .status("PAGO")
                .build();

        // Agendamento 2: R$ 500 total, R$ 50 insumos -> Base R$ 450 -> R$ 135 estúdio (30%) / R$ 315 artista 2 (70%)
        Agendamento ag2 = Agendamento.builder()
                .id(2L)
                .tatuadorId(20L)
                .valorEstimado(new BigDecimal("500.00"))
                .status("CONCLUIDO")
                .dataHora(dataSessao.plusDays(1))
                .build();

        Repasse rep2 = Repasse.builder()
                .id(102L)
                .agendamentoId(2L)
                .tatuadorId(20L)
                .custoInsumos(new BigDecimal("50.00"))
                .valorRepasse(new BigDecimal("315.00"))
                .valorLiquido(new BigDecimal("315.00"))
                .porcentagemEstudio(new BigDecimal("30.00"))
                .dataRepasse(dataSessao.plusDays(1))
                .status("PENDENTE")
                .build();

        when(repasseRepository.buscarPorPeriodo(any(), any())).thenReturn(List.of(rep1, rep2));
        when(agendamentoRepository.listarPorPeriodoEStatus(any(), any(), eq("CONCLUIDO"), isNull())).thenReturn(List.of(ag1, ag2));
        when(agendamentoRepository.listarPorPeriodoEStatus(any(), any(), isNull(), isNull())).thenReturn(List.of(ag1, ag2));

        Pagamento pag1 = Pagamento.builder().id(1L).valor(new BigDecimal("1000.00")).status("CONCLUIDO").tipo("PAGAMENTO").build();
        Pagamento pag2 = Pagamento.builder().id(2L).valor(new BigDecimal("500.00")).status("CONCLUIDO").tipo("PAGAMENTO").build();
        when(financeiroRepository.buscarPorPeriodo(any(), any())).thenReturn(List.of(pag1, pag2));

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(Usuario.builder().id(10L).nome("Artista Bruno").build()));
        when(usuarioRepository.findById(20L)).thenReturn(Optional.of(Usuario.builder().id(20L).nome("Artista Carla").build()));

        ItemEstoque critico = ItemEstoque.builder().id(1L).nome("Tinta Preta").quantidade(2).quantidadeMinima(5).build();
        when(estoqueRepository.count()).thenReturn(10L);
        when(estoqueRepository.buscarItensEmEstoqueMinimo()).thenReturn(List.of(critico));

        // Executar
        RelatorioMensalDTO relatorio = relatorioService.gerarRelatorioMensal(11, 2026);

        // Validações principais
        assertNotNull(relatorio);
        assertEquals(new BigDecimal("1500.00"), relatorio.getTotalFaturamento());
        assertEquals(new BigDecimal("1500.00"), relatorio.getFaturamentoBruto());
        assertEquals(new BigDecimal("1500.00"), relatorio.getFaturamentoRecebido());
        assertEquals(new BigDecimal("405.00"), relatorio.getTotalComissoes()); // 270 + 135
        assertEquals(new BigDecimal("945.00"), relatorio.getTotalRepasses());  // 630 + 315
        assertEquals(new BigDecimal("150.00"), relatorio.getTotalGastos());    // 100 + 50
        assertEquals(new BigDecimal("150.00"), relatorio.getTotalGastosInsumos());
        assertEquals(new BigDecimal("405.00"), relatorio.getLucroLiquido());
        assertEquals(2, relatorio.getTotalSessoes());
        assertEquals(2, relatorio.getTotalSessoesConcluidas());

        // Validação detalhamento de artistas
        assertEquals(2, relatorio.getDetalhamentoArtistas().size());
        RelatorioArtistaDTO art1 = relatorio.getDetalhamentoArtistas().stream()
                .filter(a -> a.getTatuadorId().equals(10L))
                .findFirst().orElseThrow();
        assertEquals("Artista Bruno", art1.getNomeTatuador());
        assertEquals(new BigDecimal("1000.00"), art1.getTotalFaturado());
        assertEquals(new BigDecimal("900.00"), art1.getBaseCalculo());
        assertEquals(new BigDecimal("270.00"), art1.getComissaoEstudio());
        assertEquals(new BigDecimal("630.00"), art1.getRepasseArtista());
        assertEquals(new BigDecimal("100.00"), art1.getCustoInsumos());

        // Validação detalhamento de repasses
        assertEquals(2, relatorio.getDetalhamentoRepasses().size());

        // Validação estoque
        assertEquals(10L, relatorio.getResumoEstoque().getTotalItensCadastrados());
        assertEquals(1L, relatorio.getResumoEstoque().getItensEmEstoqueCritico());
        assertTrue(relatorio.getResumoEstoque().isAlertaEstoqueAtivo());
    }

    @Test
    @DisplayName("[F8-BACK-01] Deve derivar comissão quando repasse não possui agendamento vinculado")
    void deveDerivarComissaoSemAgendamento() {
        LocalDateTime agora = LocalDateTime.of(2026, 11, 20, 10, 0);

        // Repasse avulso: Artista R$ 700.00 (70% de 1000), insumos R$ 50.00
        // Base = 700 / 0.70 = 1000. Comissao = 300. Total = 1050.
        Repasse rep = Repasse.builder()
                .id(201L)
                .agendamentoId(null)
                .tatuadorId(30L)
                .custoInsumos(new BigDecimal("50.00"))
                .valorRepasse(new BigDecimal("700.00"))
                .valorLiquido(new BigDecimal("700.00"))
                .dataRepasse(agora)
                .status("PAGO")
                .build();

        when(repasseRepository.buscarPorPeriodo(any(), any())).thenReturn(List.of(rep));
        when(agendamentoRepository.listarPorPeriodoEStatus(any(), any(), any(), isNull())).thenReturn(Collections.emptyList());
        when(financeiroRepository.buscarPorPeriodo(any(), any())).thenReturn(Collections.emptyList());
        when(usuarioRepository.findById(30L)).thenReturn(Optional.empty());
        when(estoqueRepository.count()).thenReturn(0L);
        when(estoqueRepository.buscarItensEmEstoqueMinimo()).thenReturn(Collections.emptyList());

        RelatorioMensalDTO relatorio = relatorioService.gerarRelatorioMensal(11, 2026);

        assertEquals(new BigDecimal("1050.00"), relatorio.getTotalFaturamento());
        assertEquals(new BigDecimal("300.00"), relatorio.getTotalComissoes());
        assertEquals(new BigDecimal("700.00"), relatorio.getTotalRepasses());
        assertEquals(new BigDecimal("50.00"), relatorio.getTotalGastos());
        assertEquals(1, relatorio.getDetalhamentoArtistas().size());
        assertEquals("Tatuador #30", relatorio.getDetalhamentoArtistas().get(0).getNomeTatuador());
    }
}

