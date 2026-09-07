package com.tattooflow.modules.termo;

import com.tattooflow.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TermoService {

    private final TermoRepository termoRepository;
    private final PdfGeneratorService pdfGeneratorService;

    public TermoService(TermoRepository termoRepository, PdfGeneratorService pdfGeneratorService) {
        this.termoRepository = termoRepository;
        this.pdfGeneratorService = pdfGeneratorService;
    }

    public List<TermoAceite> listarTodos() {
        return termoRepository.findAll();
    }

    public TermoAceite buscarPorId(Long id) {
        return termoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Termo não encontrado com ID: " + id));
    }

    public TermoAceite salvar(TermoAceite termo) {
        return termoRepository.save(termo);
    }
}
