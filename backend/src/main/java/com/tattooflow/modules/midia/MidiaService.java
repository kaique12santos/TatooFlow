package com.tattooflow.modules.midia;

import com.tattooflow.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MidiaService {

    private final MidiaRepository midiaRepository;

    public MidiaService(MidiaRepository midiaRepository) {
        this.midiaRepository = midiaRepository;
    }

    public List<Midia> listarPorAgendamento(Long agendamentoId) {
        return midiaRepository.findByAgendamentoId(agendamentoId);
    }

    public Midia salvar(Midia midia) {
        return midiaRepository.save(midia);
    }
}
