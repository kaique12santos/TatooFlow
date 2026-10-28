package com.tattooflow.modules.midia;

import com.tattooflow.common.exception.BusinessException;
import com.tattooflow.modules.agendamento.AgendamentoRepository;
import com.tattooflow.modules.termo.TermoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MidiaService {

    private final MidiaRepository midiaRepository;
    private TermoRepository termoRepository;
    private AgendamentoRepository agendamentoRepository;

    public MidiaService(MidiaRepository midiaRepository) {
        this.midiaRepository = midiaRepository;
        this.termoRepository = null;
        this.agendamentoRepository = null;
    }

    @Autowired
    public MidiaService(MidiaRepository midiaRepository,
                        TermoRepository termoRepository,
                        AgendamentoRepository agendamentoRepository) {
        this.midiaRepository = midiaRepository;
        this.termoRepository = termoRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    public List<Midia> listarPorAgendamento(Long agendamentoId) {
        return midiaRepository.findByAgendamentoId(agendamentoId);
    }

    /**
     * Salva o registro da mídia no sistema.
     * Critério de aceite [F3-BACK-02]: Bloqueio de upload de mídia (Fase 4) sem aceite prévio do termo de consentimento.
     *
     * @param midia Objeto com os dados da mídia a ser enviada
     * @return Mídia persistida
     */
    public Midia salvar(Midia midia) {
        if (midia == null) {
            throw new BusinessException("Os dados da mídia são obrigatórios.");
        }
        if (midia.getAgendamentoId() == null) {
            throw new BusinessException("agendamentoId é obrigatório para envio de mídia.");
        }
        if (midia.getUrl() == null || midia.getUrl().isBlank()) {
            throw new BusinessException("url da mídia é obrigatória.");
        }

        // Validação da Fase 4: Bloqueio de upload de mídia sem aceite prévio
        validarAceitePrevio(midia.getAgendamentoId());

        if (midia.getDataUpload() == null) {
            midia.setDataUpload(LocalDateTime.now());
        }

        return midiaRepository.save(midia);
    }

    private void validarAceitePrevio(Long agendamentoId) {
        if (termoRepository == null) {
            return;
        }

        // 1. Verifica aceite assinado vinculado diretamente ao agendamento
        boolean aceitePorAgendamento = termoRepository.existsByAgendamentoIdAndDataAssinaturaIsNotNull(agendamentoId);
        if (aceitePorAgendamento) {
            return;
        }

        // 2. Se não encontrou por agendamento, verifica pelo cliente correspondente
        if (agendamentoRepository != null) {
            var agendamentoOpt = agendamentoRepository.findById(agendamentoId);
            if (agendamentoOpt.isPresent() && agendamentoOpt.get().getClienteId() != null) {
                Long clienteId = agendamentoOpt.get().getClienteId();
                if (termoRepository.existsByClienteIdAndDataAssinaturaIsNotNull(clienteId)) {
                    return;
                }
            }
        }

        // Bloqueio rigoroso da Fase 4
        throw new BusinessException("Upload de mídia bloqueado: o cliente deve aceitar digitalmente o termo de consentimento antes de enviar fotos (Fase 4).");
    }
}
