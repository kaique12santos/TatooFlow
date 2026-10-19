package com.tattooflow.modules.agendamento;

import com.tattooflow.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository repository;

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
} 