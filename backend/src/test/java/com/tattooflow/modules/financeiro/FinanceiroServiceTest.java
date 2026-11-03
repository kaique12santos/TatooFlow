package com.tattooflow.modules.financeiro;

import com.tattooflow.common.exception.BusinessException;
import com.tattooflow.common.exception.ResourceNotFoundException;
import com.tattooflow.modules.agendamento.Agendamento;
import com.tattooflow.modules.agendamento.AgendamentoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinanceiroServiceTest {

    @Mock
    private FinanceiroRepository financeiroRepository;

    @Mock
    private RepasseRepository repasseRepository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @InjectMocks
    private FinanceiroService financeiroService;

    @Test
    void deveListarPagamentosComSucesso() {
        Pagamento mockPagamento = Pagamento.builder().id(1L).valor(new BigDecimal("350.00")).status("CONCLUIDO").build();
        when(financeiroRepository.findAll()).thenReturn(List.of(mockPagamento));

        List<Pagamento> pagamentos = financeiroService.listarPagamentos();

        assertNotNull(pagamentos);
        assertEquals(1, pagamentos.size());
        verify(financeiroRepository, times(1)).findAll();
    }

    @Test
    void deveBloquearSinalPeloRegistroGenerico() {
        assertThrows(BusinessException.class, () -> financeiroService.registrarPagamento(
                Pagamento.builder().tipo("SINAL").build()));
        verifyNoInteractions(financeiroRepository);
    }

    @Test
    void deveBloquearSobrescritaDePagamentoPeloRegistroGenerico() {
        assertThrows(BusinessException.class, () -> financeiroService.registrarPagamento(
                Pagamento.builder().id(1L).build()));
        verifyNoInteractions(financeiroRepository);
    }

    // =========================================================================
    // [F6-BACK-01] TESTES DA REGRA DE DIVISÃO 30/70 (RN02) E INSUMOS (RN07)
    // =========================================================================

    @Test
    @DisplayName("[F6-BACK-01] Deve calcular divisão 30% estúdio / 70% artista sem custo de insumos (RN02)")
    void deveCalcularDivisao3070SemInsumos() {
        BigDecimal total = new BigDecimal("500.00");
        DivisaoSessaoDTO dto = financeiroService.calcularDivisao(total, null);

        assertThat(dto.getValorTotal()).isEqualByComparingTo("500.00");
        assertThat(dto.getCustoInsumos()).isEqualByComparingTo("0.00");
        assertThat(dto.getBaseCalculo()).isEqualByComparingTo("500.00");
        assertThat(dto.getPorcentagemEstudio()).isEqualByComparingTo("30.00");
        assertThat(dto.getValorEstudio()).isEqualByComparingTo("150.00");
        assertThat(dto.getPorcentagemArtista()).isEqualByComparingTo("70.00");
        assertThat(dto.getValorArtista()).isEqualByComparingTo("350.00");
        assertThat(dto.getValorEstudio().add(dto.getValorArtista())).isEqualByComparingTo(dto.getBaseCalculo());
    }

    @Test
    @DisplayName("[F6-BACK-01] Deve deduzir o custo de insumos ANTES da divisão 30/70 (RN07 e RN02)")
    void deveCalcularDivisao3070ComDeducaoPreviaDeInsumosRN07() {
        // Exemplo: Total R$ 600,00, Insumos R$ 100,00 -> Base = R$ 500,00
        // Estúdio (30%) = R$ 150,00
        // Artista (70%) = R$ 350,00
        BigDecimal total = new BigDecimal("600.00");
        BigDecimal insumos = new BigDecimal("100.00");

        DivisaoSessaoDTO dto = financeiroService.calcularDivisao(total, insumos);

        assertThat(dto.getValorTotal()).isEqualByComparingTo("600.00");
        assertThat(dto.getCustoInsumos()).isEqualByComparingTo("100.00");
        assertThat(dto.getBaseCalculo()).isEqualByComparingTo("500.00");
        assertThat(dto.getValorEstudio()).isEqualByComparingTo("150.00");
        assertThat(dto.getValorArtista()).isEqualByComparingTo("350.00");
        assertThat(dto.getValorEstudio().add(dto.getValorArtista())).isEqualByComparingTo(dto.getBaseCalculo());
    }

    @Test
    @DisplayName("[F6-BACK-01] Deve garantir exatidão de soma e arredondamento em centavos fracionários")
    void deveGarantirSomaExataComCentavosFracionarios() {
        BigDecimal total = new BigDecimal("100.01");
        BigDecimal insumos = BigDecimal.ZERO;

        DivisaoSessaoDTO dto = financeiroService.calcularDivisao(total, insumos);

        // 100.01 * 0.30 = 30.003 -> 30.00
        // 100.01 - 30.00 = 70.01 -> Artista 70.01
        assertThat(dto.getValorEstudio()).isEqualByComparingTo("30.00");
        assertThat(dto.getValorArtista()).isEqualByComparingTo("70.01");
        assertThat(dto.getValorEstudio().add(dto.getValorArtista())).isEqualByComparingTo(total);
    }

    @Test
    @DisplayName("[F6-BACK-01] Deve rejeitar valor total negativo ou zero")
    void deveRejeitarValorTotalNegativoOuZero() {
        assertThrows(BusinessException.class, () -> financeiroService.calcularDivisao(BigDecimal.ZERO, BigDecimal.ZERO));
        assertThrows(BusinessException.class, () -> financeiroService.calcularDivisao(new BigDecimal("-10.00"), BigDecimal.ZERO));
        assertThrows(BusinessException.class, () -> financeiroService.calcularDivisao(null, BigDecimal.ZERO));
    }

    @Test
    @DisplayName("[F6-BACK-01] Deve rejeitar custo de insumos negativo ou maior que o valor total")
    void deveRejeitarCustoInsumosInvalido() {
        BigDecimal total = new BigDecimal("400.00");
        assertThrows(BusinessException.class, () -> financeiroService.calcularDivisao(total, new BigDecimal("-1.00")));
        assertThrows(BusinessException.class, () -> financeiroService.calcularDivisao(total, new BigDecimal("450.00")));
    }

    @Test
    @DisplayName("[F6-BACK-01] Deve finalizar sessão, salvar Repasse de 70% com insumos e atualizar agendamento para CONCLUIDO")
    void deveFinalizarSessaoECriarRepasseComSucesso() {
        Long agendamentoId = 15L;
        Agendamento agendamento = Agendamento.builder()
                .id(agendamentoId)
                .tatuadorId(5L)
                .clienteId(2L)
                .valorEstimado(new BigDecimal("700.00"))
                .status("CONFIRMADO")
                .dataHora(LocalDateTime.now())
                .duracaoMinutos(90)
                .build();

        when(agendamentoRepository.buscarPorIdParaAtualizacao(agendamentoId)).thenReturn(Optional.of(agendamento));
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(i -> i.getArgument(0));
        when(repasseRepository.save(any(Repasse.class))).thenAnswer(i -> {
            Repasse r = i.getArgument(0);
            r.setId(99L);
            return r;
        });

        FinalizarSessaoResponse response = financeiroService.finalizarSessao(agendamentoId, new BigDecimal("100.00"), null);

        assertThat(response).isNotNull();
        assertThat(response.getAgendamento().getStatus()).isEqualTo("CONCLUIDO");
        assertThat(response.getDivisao().getBaseCalculo()).isEqualByComparingTo("600.00");
        assertThat(response.getDivisao().getValorEstudio()).isEqualByComparingTo("180.00");
        assertThat(response.getDivisao().getValorArtista()).isEqualByComparingTo("420.00");

        ArgumentCaptor<Repasse> repasseCaptor = ArgumentCaptor.forClass(Repasse.class);
        verify(repasseRepository).save(repasseCaptor.capture());
        Repasse salvo = repasseCaptor.getValue();
        assertThat(salvo.getAgendamentoId()).isEqualTo(agendamentoId);
        assertThat(salvo.getTatuadorId()).isEqualTo(5L);
        assertThat(salvo.getValorRepasse()).isEqualByComparingTo("420.00");
        assertThat(salvo.getCustoInsumos()).isEqualByComparingTo("100.00");
        assertThat(salvo.getPorcentagemEstudio()).isEqualByComparingTo("30.00");
        assertThat(salvo.getStatus()).isEqualTo("PENDENTE");
    }

    @Test
    @DisplayName("[F6-BACK-01] Deve bloquear finalização de agendamento cancelado ou não confirmado")
    void deveBloquearFinalizacaoInvalida() {
        Agendamento cancelado = Agendamento.builder().id(1L).status("CANCELADO").build();
        when(agendamentoRepository.buscarPorIdParaAtualizacao(1L)).thenReturn(Optional.of(cancelado));
        assertThrows(BusinessException.class, () -> financeiroService.finalizarSessao(1L, null, null));

        Agendamento apenasAgendado = Agendamento.builder().id(2L).status("AGENDADO").build();
        when(agendamentoRepository.buscarPorIdParaAtualizacao(2L)).thenReturn(Optional.of(apenasAgendado));
        assertThrows(BusinessException.class, () -> financeiroService.finalizarSessao(2L, null, null));
    }

    @Test
    @DisplayName("[F6-BACK-01] Deve bloquear finalização duplicada quando já existe repasse registrado")
    void deveBloquearFinalizacaoDuplicadaComRepasseExistente() {
        Agendamento concluido = Agendamento.builder().id(3L).status("CONCLUIDO").valorEstimado(new BigDecimal("500.00")).build();
        when(agendamentoRepository.buscarPorIdParaAtualizacao(3L)).thenReturn(Optional.of(concluido));
        when(repasseRepository.findFirstByAgendamentoIdOrderByDataRepasseDesc(3L))
                .thenReturn(Optional.of(Repasse.builder().id(1L).agendamentoId(3L).build()));

        assertThrows(BusinessException.class, () -> financeiroService.finalizarSessao(3L, null, null));
    }

    // =========================================================================
    // [F6-BACK-02] TESTES DA REGRA DE CANCELAMENTO E REEMBOLSO (RN05)
    // =========================================================================

    @Test
    @DisplayName("[F6-BACK-02] Deve permitir reembolso integral de 100% quando cancelado com 48h ou mais de antecedência (RN05)")
    void devePermitirReembolsoIntegralQuandoCanceladoCom48hOuMais() {
        Long agendamentoId = 20L;
        LocalDateTime sessao = LocalDateTime.of(2026, 11, 10, 14, 0);
        LocalDateTime agora = LocalDateTime.of(2026, 11, 7, 14, 0); // 72 horas de antecedência

        Agendamento agendamento = Agendamento.builder()
                .id(agendamentoId)
                .dataHora(sessao)
                .valorEstimado(new BigDecimal("400.00"))
                .status("CONFIRMADO")
                .build();

        Pagamento sinal = Pagamento.builder()
                .agendamentoId(agendamentoId)
                .valor(new BigDecimal("200.00"))
                .status("CONCLUIDO")
                .tipo("SINAL")
                .build();

        when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));
        when(financeiroRepository.findByAgendamentoId(agendamentoId)).thenReturn(List.of(sinal));

        CalculoReembolsoResponse res = financeiroService.calcularReembolso(agendamentoId, agora, null);

        assertThat(res.isMenosDe48Horas()).isFalse();
        assertThat(res.isReembolsoIntegralPermitido()).isTrue();
        assertThat(res.getHorasAntecedencia()).isEqualTo(72);
        assertThat(res.getValorPago()).isEqualByComparingTo("200.00");
        assertThat(res.getPercentualDesconto()).isEqualByComparingTo("0.00");
        assertThat(res.getValorDesconto()).isEqualByComparingTo("0.00");
        assertThat(res.getValorReembolsavel()).isEqualByComparingTo("200.00");
        assertThat(res.getPolitica()).isEqualTo("RN05");
    }

    @Test
    @DisplayName("[F6-BACK-02] Deve bloquear reembolso integral e aplicar desconto de 50% quando cancelamento for com menos de 48h (RN05)")
    void deveBloquearReembolsoIntegralEAplicarDescontoQuandoMenosDe48h() {
        Long agendamentoId = 21L;
        LocalDateTime sessao = LocalDateTime.of(2026, 11, 10, 14, 0);
        LocalDateTime agora = LocalDateTime.of(2026, 11, 9, 10, 0); // 28 horas de antecedência (< 48h)

        Agendamento agendamento = Agendamento.builder()
                .id(agendamentoId)
                .dataHora(sessao)
                .valorEstimado(new BigDecimal("500.00"))
                .status("CONFIRMADO")
                .build();

        Pagamento sinal = Pagamento.builder()
                .agendamentoId(agendamentoId)
                .valor(new BigDecimal("250.00"))
                .status("CONCLUIDO")
                .tipo("SINAL")
                .build();

        when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));
        when(financeiroRepository.findByAgendamentoId(agendamentoId)).thenReturn(List.of(sinal));

        CalculoReembolsoResponse res = financeiroService.calcularReembolso(agendamentoId, agora, null);

        assertThat(res.isMenosDe48Horas()).isTrue();
        assertThat(res.isReembolsoIntegralPermitido()).isFalse();
        assertThat(res.getHorasAntecedencia()).isEqualTo(28);
        assertThat(res.getValorPago()).isEqualByComparingTo("250.00");
        assertThat(res.getPercentualDesconto()).isEqualByComparingTo("50.00");
        assertThat(res.getValorDesconto()).isEqualByComparingTo("125.00");
        assertThat(res.getValorReembolsavel()).isEqualByComparingTo("125.00");
    }

    @Test
    @DisplayName("[F6-BACK-02] Deve lançar BusinessException ao tentar forçar reembolso integral quando antecedência for < 48h (RN05)")
    void deveLancarExcecaoAoForcarReembolsoIntegralComMenosDe48h() {
        Long agendamentoId = 22L;
        LocalDateTime sessao = LocalDateTime.of(2026, 11, 10, 14, 0);
        LocalDateTime agora = LocalDateTime.of(2026, 11, 9, 14, 0); // 24 horas

        Agendamento agendamento = Agendamento.builder()
                .id(agendamentoId)
                .dataHora(sessao)
                .valorEstimado(new BigDecimal("300.00"))
                .status("CONFIRMADO")
                .build();

        when(agendamentoRepository.buscarPorIdParaAtualizacao(agendamentoId)).thenReturn(Optional.of(agendamento));

        CancelarAgendamentoRequest request = CancelarAgendamentoRequest.builder()
                .dataCancelamento(agora)
                .forcarReembolsoIntegral(true)
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () ->
                financeiroService.cancelarAgendamento(agendamentoId, request));

        assertThat(ex.getMessage()).contains("Cancelamentos com menos de 48 horas de antecedência não têm direito a reembolso integral (RN05)");
    }

    @Test
    @DisplayName("[F6-BACK-02] Deve lançar BusinessException quando desconto for zero ou negativo em cancelamento com menos de 48h (RN05)")
    void deveLancarExcecaoAoInformarDescontoZeroComMenosDe48h() {
        Long agendamentoId = 23L;
        LocalDateTime sessao = LocalDateTime.of(2026, 11, 10, 14, 0);
        LocalDateTime agora = LocalDateTime.of(2026, 11, 9, 14, 0);

        Agendamento agendamento = Agendamento.builder()
                .id(agendamentoId)
                .dataHora(sessao)
                .valorEstimado(new BigDecimal("300.00"))
                .status("CONFIRMADO")
                .build();

        when(agendamentoRepository.buscarPorIdParaAtualizacao(agendamentoId)).thenReturn(Optional.of(agendamento));

        CancelarAgendamentoRequest request = CancelarAgendamentoRequest.builder()
                .dataCancelamento(agora)
                .percentualDesconto(BigDecimal.ZERO)
                .build();

        assertThrows(BusinessException.class, () -> financeiroService.cancelarAgendamento(agendamentoId, request));
    }

    @Test
    @DisplayName("[F6-BACK-02] Não deve conceder reembolso quando o horário da sessão já estiver no passado")
    void deveRetornarZeroReembolsoQuandoSessaoJaPassou() {
        Long agendamentoId = 24L;
        LocalDateTime sessao = LocalDateTime.of(2026, 11, 1, 10, 0);
        LocalDateTime agora = LocalDateTime.of(2026, 11, 1, 12, 0); // 2h depois

        Agendamento agendamento = Agendamento.builder()
                .id(agendamentoId)
                .dataHora(sessao)
                .valorEstimado(new BigDecimal("400.00"))
                .status("CONFIRMADO")
                .build();

        when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));
        when(financeiroRepository.findByAgendamentoId(agendamentoId)).thenReturn(List.of(
                Pagamento.builder().agendamentoId(agendamentoId).valor(new BigDecimal("200.00")).status("CONCLUIDO").build()
        ));

        CalculoReembolsoResponse res = financeiroService.calcularReembolso(agendamentoId, agora, null);

        assertThat(res.isReembolsoIntegralPermitido()).isFalse();
        assertThat(res.getValorReembolsavel()).isEqualByComparingTo("0.00");
        assertThat(res.getValorDesconto()).isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("[F6-BACK-02] Deve cancelar agendamento e persistir estorno de reembolso no financeiro")
    void deveCancelarAgendamentoEPersistirPagamentoDeReembolso() {
        Long agendamentoId = 25L;
        LocalDateTime sessao = LocalDateTime.of(2026, 11, 10, 14, 0);
        LocalDateTime agora = LocalDateTime.of(2026, 11, 9, 14, 0); // 24h -> 50% de R$ 200,00 = R$ 100,00

        Agendamento agendamento = Agendamento.builder()
                .id(agendamentoId)
                .dataHora(sessao)
                .valorEstimado(new BigDecimal("400.00"))
                .status("CONFIRMADO")
                .build();

        Pagamento sinal = Pagamento.builder()
                .agendamentoId(agendamentoId)
                .valor(new BigDecimal("200.00"))
                .status("CONCLUIDO")
                .tipo("SINAL")
                .build();

        when(agendamentoRepository.buscarPorIdParaAtualizacao(agendamentoId)).thenReturn(Optional.of(agendamento));
        when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));
        when(financeiroRepository.findByAgendamentoId(agendamentoId)).thenReturn(List.of(sinal));
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(i -> i.getArgument(0));
        when(financeiroRepository.save(any(Pagamento.class))).thenAnswer(i -> {
            Pagamento p = i.getArgument(0);
            p.setId(88L);
            return p;
        });

        CancelarAgendamentoRequest req = CancelarAgendamentoRequest.builder()
                .dataCancelamento(agora)
                .motivo("Imprevisto pessoal")
                .build();

        CancelamentoResponse response = financeiroService.cancelarAgendamento(agendamentoId, req);

        assertThat(response.getAgendamento().getStatus()).isEqualTo("CANCELADO");
        assertThat(response.getReembolso().getValorReembolsavel()).isEqualByComparingTo("100.00");
        assertThat(response.getPagamentoReembolso()).isNotNull();
        assertThat(response.getPagamentoReembolso().getTipo()).isEqualTo("REEMBOLSO");
        assertThat(response.getPagamentoReembolso().getValor()).isEqualByComparingTo("100.00");

        ArgumentCaptor<Pagamento> pagCaptor = ArgumentCaptor.forClass(Pagamento.class);
        verify(financeiroRepository).save(pagCaptor.capture());
        Pagamento pSalvo = pagCaptor.getValue();
        assertThat(pSalvo.getTipo()).isEqualTo("REEMBOLSO");
        assertThat(pSalvo.getStatus()).isEqualTo("CONCLUIDO");
    }

    @Test
    @DisplayName("[F6-BACK-02] Deve bloquear cancelamento de agendamento já cancelado ou já concluído")
    void deveBloquearCancelamentoInvalido() {
        Agendamento cancelado = Agendamento.builder().id(30L).status("CANCELADO").build();
        when(agendamentoRepository.buscarPorIdParaAtualizacao(30L)).thenReturn(Optional.of(cancelado));
        assertThrows(BusinessException.class, () -> financeiroService.cancelarAgendamento(30L, null));

        Agendamento concluido = Agendamento.builder().id(31L).status("CONCLUIDO").build();
        when(agendamentoRepository.buscarPorIdParaAtualizacao(31L)).thenReturn(Optional.of(concluido));
        assertThrows(BusinessException.class, () -> financeiroService.cancelarAgendamento(31L, null));
    }
}
