package com.tattooflow.modules.termo;

import com.tattooflow.common.exception.BusinessException;
import com.tattooflow.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TermoService {

    public static final int ANOS_MINIMOS_RETENCAO = 5;

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
        if (termo.getClienteId() == null) {
            throw new BusinessException("clienteId é obrigatório para registrar o termo de aceite.");
        }
        if (termo.getAgendamentoId() == null) {
            throw new BusinessException("agendamentoId é obrigatório para registrar o termo de aceite.");
        }
        if (termo.getUrlPdfAssinado() == null || termo.getUrlPdfAssinado().isBlank()) {
            throw new BusinessException("urlPdfAssinado é obrigatório para registrar o termo de aceite.");
        }

        if (termo.getDataAssinatura() == null) {
            termo.setDataAssinatura(LocalDateTime.now());
        }

        LocalDateTime retencaoMinima = termo.getDataAssinatura().plusYears(ANOS_MINIMOS_RETENCAO);
        if (termo.getDataExpiracaoRetencao() == null) {
            // RN06: Prazo mínimo de retenção legal de 5 anos a partir do aceite
            termo.setDataExpiracaoRetencao(retencaoMinima);
        } else if (termo.getDataExpiracaoRetencao().isBefore(retencaoMinima)) {
            throw new BusinessException("RN06: O prazo de expiração da retenção deve ser de no mínimo "
                    + ANOS_MINIMOS_RETENCAO + " anos a partir da data de assinatura.");
        }

        return termoRepository.save(termo);
    }
}
