package com.tattooflow.modules.agendamento;

import com.tattooflow.common.exception.BusinessException;
import com.tattooflow.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AgendamentoService {

    private static final int DURACAO_PADRAO_MINUTOS = 60;

    private final AgendamentoRepository agendamentoRepository;

    public AgendamentoService(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    public List<Agendamento> listarTodos() {
        return agendamentoRepository.findAll();
    }

    public List<Agendamento> listarPorTatuador(Long tatuadorId) {
        return agendamentoRepository.findByTatuadorId(tatuadorId);
    }

    public Agendamento buscarPorId(Long id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + id));
    }

    public Agendamento salvar(Agendamento agendamento) {
        if (agendamento.getClienteId() == null || agendamento.getTatuadorId() == null
                || agendamento.getDataHora() == null) {
            throw new BusinessException("Cliente, tatuador e data/hora são obrigatórios");
        }
        if (agendamento.getDuracaoMinutos() == null) {
            agendamento.setDuracaoMinutos(DURACAO_PADRAO_MINUTOS);
        }
        if (agendamento.getDuracaoMinutos() <= 0) {
            throw new BusinessException("A duração deve ser maior que zero");
        }
        if (agendamento.getStatus() == null) {
            agendamento.setStatus("AGENDADO");
        }

        if (!"CANCELADO".equals(agendamento.getStatus())) {
            validarConflito(agendamento);
        }
        return agendamentoRepository.save(agendamento);
    }

    private void validarConflito(Agendamento novo) {
        LocalDateTime inicio = novo.getDataHora();
        LocalDateTime fim = inicio.plusMinutes(novo.getDuracaoMinutos());

        List<Agendamento> candidatos = agendamentoRepository
                .buscarCandidatosConflito(novo.getTatuadorId(), inicio.minusHours(24), fim);

        boolean conflito = candidatos.stream()
                .filter(e -> novo.getId() == null || !novo.getId().equals(e.getId()))
                .anyMatch(e -> {
                    int duracao = e.getDuracaoMinutos() != null ? e.getDuracaoMinutos() : DURACAO_PADRAO_MINUTOS;
                    LocalDateTime eFim = e.getDataHora().plusMinutes(duracao);
                    return e.getDataHora().isBefore(fim) && eFim.isAfter(inicio);
                });

        if (conflito) {
            throw new BusinessException("Tatuador já possui agendamento nesse horário");
        }
    }
}