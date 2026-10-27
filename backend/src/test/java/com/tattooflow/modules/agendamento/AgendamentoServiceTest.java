package com.tattooflow.modules.agendamento;

import com.tattooflow.common.exception.BusinessException;
import com.tattooflow.modules.financeiro.FinanceiroRepository;
import com.tattooflow.modules.financeiro.Pagamento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository repository;

    @Mock
    private FinanceiroRepository pagamentos;

    @InjectMocks
    private AgendamentoService service;

    private final LocalDateTime base = LocalDateTime.of(2026, 10, 10, 14, 0);

    private Agendamento agendamento(Long id, LocalDateTime inicio, int duracao) {
        return Agendamento.builder()
                .id(id).clienteId(1L).tatuadorId(1L)
                .dataHora(inicio).duracaoMinutos(duracao).status("AGENDADO")
                .build();
    }

    @Test
    void deveLancarExcecaoQuandoHorarioSobrepoe() {
        Agendamento existente = agendamento(10L, base, 120);          // 14h às 16h
        when(repository.buscarCandidatosConflito(eq(1L), any(), any())).thenReturn(List.of(existente));

        Agendamento novo = agendamento(null, base.plusMinutes(60), 60); // 15h às 16h

        assertThrows(BusinessException.class, () -> service.salvar(novo));
        verify(repository, never()).save(any());
    }

    @Test
    void devePermitirSessoesColadas() {
        Agendamento existente = agendamento(10L, base, 120);          // 14h às 16h
        when(repository.buscarCandidatosConflito(eq(1L), any(), any())).thenReturn(List.of(existente));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Agendamento novo = agendamento(null, base.plusMinutes(120), 60); // 16h às 17h

        assertDoesNotThrow(() -> service.salvar(novo));
        verify(repository).save(novo);
    }

    @Test
    void naoDeveConflitarComELeMesmoAoEditar() {
        Agendamento existente = agendamento(10L, base, 60);
        when(repository.buscarPorIdParaAtualizacao(10L)).thenReturn(Optional.of(existente));
        when(repository.buscarCandidatosConflito(eq(1L), any(), any())).thenReturn(List.of(existente));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> service.salvar(agendamento(10L, base, 90)));
    }

    @Test
    void deveAplicarDuracaoEStatusPadrao() {
        when(repository.buscarCandidatosConflito(any(), any(), any())).thenReturn(List.of());
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Agendamento novo = Agendamento.builder().clienteId(1L).tatuadorId(1L).dataHora(base).build();
        Agendamento salvo = service.salvar(novo);

        assertEquals(60, salvo.getDuracaoMinutos());
        assertEquals("AGENDADO", salvo.getStatus());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CONFIRMADO", "EM_ANDAMENTO", "CONCLUIDO", "Confirmado", " confirmado "})
    void deveImpedirConfirmacaoSemSinalPeloCadastro(String status) {
        Agendamento novo = agendamento(null, base, 60);
        novo.setStatus(status);

        assertThrows(BusinessException.class, () -> service.salvar(novo));
        verify(repository, never()).save(any());
    }

    @Test
    void deveImpedirConfirmacaoSemSinalPelaEdicao() {
        when(repository.buscarPorIdParaAtualizacao(10L)).thenReturn(Optional.of(agendamento(10L, base, 60)));
        Agendamento editar = agendamento(10L, base, 60);
        editar.setStatus("CONFIRMADO");

        assertThrows(BusinessException.class, () -> service.salvar(editar));
        verify(repository, never()).save(any());
    }

    @Test
    void devePreservarConfirmacaoAoEditarAgendamentoComSinal() {
        Agendamento existente = agendamento(10L, base, 60);
        existente.setStatus("CONFIRMADO");
        when(repository.buscarPorIdParaAtualizacao(10L)).thenReturn(Optional.of(existente));
        when(pagamentos.findByAgendamentoIdAndTipo(10L, "SINAL")).thenReturn(Optional.of(sinalRegistrado()));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        Agendamento editar = agendamento(10L, base, 90);
        editar.setStatus(null);
        editar.setValorEstimado(new BigDecimal("350.00"));

        assertEquals("CONFIRMADO", service.salvar(editar).getStatus());
    }

    @Test
    void deveRecusarAlteracaoDeValorQueInvalideSinalRegistrado() {
        when(repository.buscarPorIdParaAtualizacao(10L)).thenReturn(Optional.of(agendamento(10L, base, 60)));
        when(pagamentos.findByAgendamentoIdAndTipo(10L, "SINAL")).thenReturn(Optional.of(sinalRegistrado()));
        Agendamento editar = agendamento(10L, base, 60);
        editar.setValorEstimado(new BigDecimal("500.00"));

        assertThrows(BusinessException.class, () -> service.salvar(editar));
        verify(repository, never()).save(any());
    }

    private Pagamento sinalRegistrado() {
        return Pagamento.builder().tipo("SINAL").status("CONCLUIDO")
                .valor(new BigDecimal("175.00")).comprovante(new byte[]{1}).build();
    }
}
