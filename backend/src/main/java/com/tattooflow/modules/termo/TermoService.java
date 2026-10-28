package com.tattooflow.modules.termo;

import com.tattooflow.common.exception.BusinessException;
import com.tattooflow.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TermoService {

    public static final int ANOS_MINIMOS_RETENCAO = 5;

    private final TermoRepository termoRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private TermoStorageService termoStorageService;

    public TermoService(TermoRepository termoRepository, PdfGeneratorService pdfGeneratorService) {
        this.termoRepository = termoRepository;
        this.pdfGeneratorService = pdfGeneratorService;
        this.termoStorageService = new TermoStorageService("storage/termos");
    }

    @Autowired
    public TermoService(TermoRepository termoRepository,
                        PdfGeneratorService pdfGeneratorService,
                        TermoStorageService termoStorageService) {
        this.termoRepository = termoRepository;
        this.pdfGeneratorService = pdfGeneratorService;
        this.termoStorageService = termoStorageService;
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

    /**
     * Aceite digital do termo pelo cliente (F3-BACK-02).
     * Registra data/hora da assinatura, IP, renova retenção legal de 5 anos (RN06),
     * gera o documento PDF assinado e armazena com segurança (RN04, RES02).
     *
     * @param termoId    ID do termo
     * @param request    Dados opcionais de assinatura (IP, documento, assinatura base64)
     * @param ipFallback IP extraído da requisição HTTP
     * @return Termo de consentimento aceito e persistido
     */
    @Transactional
    public TermoAceite aceitarTermo(Long termoId, TermoAceiteRequest request, String ipFallback) {
        TermoAceite termo = buscarPorId(termoId);

        LocalDateTime agora = request != null && request.getDataAssinatura() != null
                ? request.getDataAssinatura()
                : LocalDateTime.now();

        String ip = request != null && request.getIpAssinatura() != null && !request.getIpAssinatura().isBlank()
                ? request.getIpAssinatura()
                : ipFallback;

        termo.setDataAssinatura(agora);
        termo.setIpAssinatura(ip != null ? ip : "127.0.0.1");

        // RN06: Prazo mínimo de retenção legal de 5 anos a partir do aceite
        LocalDateTime retencaoMinima = agora.plusYears(ANOS_MINIMOS_RETENCAO);
        if (termo.getDataExpiracaoRetencao() == null || termo.getDataExpiracaoRetencao().isBefore(retencaoMinima)) {
            termo.setDataExpiracaoRetencao(retencaoMinima);
        }

        // Gera o PDF assinado atualizado com os dados do cliente e data/hora do aceite
        byte[] pdfBytes = pdfGeneratorService.gerarPdfTermo(termoId);
        String caminhoArmazenado = termoStorageService.armazenar(termoId, pdfBytes);
        termo.setUrlPdfAssinado(caminhoArmazenado);

        return termoRepository.save(termo);
    }

    /**
     * Consulta termos vinculados ao histórico do cliente (Critério 3: Vínculo ao histórico do cliente).
     */
    public List<TermoAceite> listarPorCliente(Long clienteId) {
        if (clienteId == null) {
            throw new BusinessException("clienteId é obrigatório para consultar o histórico de termos.");
        }
        return termoRepository.findByClienteIdOrderByDataAssinaturaDesc(clienteId);
    }

    /**
     * Gera o documento PDF do termo de consentimento com validação de tamanho máximo de 5MB (RES02).
     *
     * @param termoId ID do termo
     * @return Conteúdo binário do PDF gerado
     */
    public byte[] gerarPdfTermo(Long termoId) {
        return pdfGeneratorService.gerarPdfTermo(termoId);
    }

    /**
     * Gera o PDF do termo e armazena com segurança no repositório de arquivos.
     *
     * @param termoId ID do termo
     * @return Termo atualizado com o caminho seguro do arquivo
     */
    @Transactional
    public TermoAceite gerarEArmazenarPdf(Long termoId) {
        TermoAceite termo = buscarPorId(termoId);
        byte[] pdfBytes = pdfGeneratorService.gerarPdfTermo(termoId);

        String caminhoSeguro = termoStorageService.armazenar(termoId, pdfBytes);
        termo.setUrlPdfAssinado(caminhoSeguro);

        return termoRepository.save(termo);
    }

    /**
     * Registra o aceite e gera automaticamente o PDF armazenado com segurança (RN04, RES02, RN06).
     */
    @Transactional
    public TermoAceite registrarComPdf(Long clienteId, Long agendamentoId, String ipAssinatura, LocalDateTime dataAssinatura) {
        if (clienteId == null) {
            throw new BusinessException("clienteId é obrigatório para registrar o termo de aceite.");
        }
        if (agendamentoId == null) {
            throw new BusinessException("agendamentoId é obrigatório para registrar o termo de aceite.");
        }

        LocalDateTime dataAceite = dataAssinatura != null ? dataAssinatura : LocalDateTime.now();

        TermoAceite termo = TermoAceite.builder()
                .clienteId(clienteId)
                .agendamentoId(agendamentoId)
                .ipAssinatura(ipAssinatura != null ? ipAssinatura : "127.0.0.1")
                .dataAssinatura(dataAceite)
                .dataExpiracaoRetencao(dataAceite.plusYears(ANOS_MINIMOS_RETENCAO))
                .urlPdfAssinado("pendente_geracao")
                .build();

        termo = termoRepository.save(termo);

        byte[] pdfBytes = pdfGeneratorService.gerarPdfTermo(termo.getId());
        String caminhoSeguro = termoStorageService.armazenar(termo.getId(), pdfBytes);
        termo.setUrlPdfAssinado(caminhoSeguro);

        return termoRepository.save(termo);
    }

    /**
     * Obtém os bytes do documento PDF para download / visualização.
     */
    @Transactional
    public byte[] obterPdf(Long termoId) {
        TermoAceite termo = buscarPorId(termoId);

        if (termoStorageService.existe(termo.getUrlPdfAssinado())) {
            return termoStorageService.carregar(termo.getUrlPdfAssinado());
        }

        byte[] pdfBytes = pdfGeneratorService.gerarPdfTermo(termoId);
        String caminhoSeguro = termoStorageService.armazenar(termoId, pdfBytes);
        termo.setUrlPdfAssinado(caminhoSeguro);
        termoRepository.save(termo);

        return pdfBytes;
    }
}
