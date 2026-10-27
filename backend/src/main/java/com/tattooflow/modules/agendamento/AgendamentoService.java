package com.tattooflow.modules.agendamento;

import com.tattooflow.common.exception.BusinessException;
import com.tattooflow.common.exception.ResourceNotFoundException;
import com.tattooflow.modules.financeiro.FinanceiroRepository;
import com.tattooflow.modules.financeiro.Pagamento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class AgendamentoService {

    private static final int DURACAO_PADRAO_MINUTOS = 60;

    private final AgendamentoRepository agendamentoRepository;
    private final FinanceiroRepository financeiroRepository;

    public AgendamentoService(AgendamentoRepository agendamentoRepository, FinanceiroRepository financeiroRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.financeiroRepository = financeiroRepository;
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

    @Transactional
    public Agendamento salvar(Agendamento agendamento) {
        Agendamento existente = null;
        Pagamento sinal = null;
        if (agendamento.getId() != null) {
            existente = agendamentoRepository.buscarPorIdParaAtualizacao(agendamento.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + agendamento.getId()));
            sinal = financeiroRepository.findByAgendamentoIdAndTipo(agendamento.getId(), "SINAL").orElse(null);
        }
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
            agendamento.setStatus(existente != null && existente.getStatus() != null ? existente.getStatus() : "AGENDADO");
        }

        validarSinal(agendamento, sinal);

        if (!"CANCELADO".equals(agendamento.getStatus())) {
            validarConflito(agendamento);
        }
        return agendamentoRepository.save(agendamento);
    }

    private void validarSinal(Agendamento agendamento, Pagamento sinal) {
        if (!Set.of("AGENDADO", "CONFIRMADO", "EM_ANDAMENTO", "CONCLUIDO", "CANCELADO").contains(agendamento.getStatus())) {
            throw new BusinessException("Status de agendamento inválido");
        }
        boolean exigeSinal = Set.of("CONFIRMADO", "EM_ANDAMENTO", "CONCLUIDO").contains(agendamento.getStatus());
        if (exigeSinal && (sinal == null || !"CONCLUIDO".equals(sinal.getStatus()) || sinal.getComprovante() == null)) {
            throw new BusinessException("A confirmação do agendamento exige o registro do sinal de 50% com comprovante");
        }
        if (sinal != null) {
            BigDecimal total = agendamento.getValorEstimado();
            if (total == null || total.signum() <= 0 || total.stripTrailingZeros().scale() > 2
                    || total.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP).compareTo(sinal.getValor()) != 0) {
                throw new BusinessException("O valor estimado deve preservar o sinal de 50% já registrado");
            }
        }
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
