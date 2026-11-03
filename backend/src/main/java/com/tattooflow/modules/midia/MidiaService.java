package com.tattooflow.modules.midia;

import com.tattooflow.common.exception.BusinessException;
import com.tattooflow.common.exception.ResourceNotFoundException;
import com.tattooflow.modules.agendamento.AgendamentoRepository;
import com.tattooflow.modules.termo.TermoRepository;
import com.tattooflow.service.GeminiService;
import com.tattooflow.service.ImagemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MidiaService {

    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    private final MidiaRepository midiaRepository;
    private TermoRepository termoRepository;
    private AgendamentoRepository agendamentoRepository;
    private ImagemService imagemService;
    private GeminiService geminiService;

    public MidiaService(MidiaRepository midiaRepository) {
        this(midiaRepository, null, null, null, null);
    }

    public MidiaService(MidiaRepository midiaRepository,
                        TermoRepository termoRepository,
                        AgendamentoRepository agendamentoRepository) {
        this(midiaRepository, termoRepository, agendamentoRepository, null, null);
    }

    public MidiaService(MidiaRepository midiaRepository,
                        TermoRepository termoRepository,
                        AgendamentoRepository agendamentoRepository,
                        ImagemService imagemService) {
        this(midiaRepository, termoRepository, agendamentoRepository, imagemService, null);
    }

    @Autowired
    public MidiaService(MidiaRepository midiaRepository,
                        TermoRepository termoRepository,
                        AgendamentoRepository agendamentoRepository,
                        ImagemService imagemService,
                        GeminiService geminiService) {
        this.midiaRepository = midiaRepository;
        this.termoRepository = termoRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.imagemService = imagemService;
        this.geminiService = geminiService;
    }

    public List<Midia> listarPorAgendamento(Long agendamentoId) {
        return midiaRepository.findByAgendamentoId(agendamentoId);
    }

    /**
     * Realiza o upload de imagem para o Cloudinary (bucket configurado) e salva a mídia no banco de dados.
     * Critérios de aceite [F4-BACK-01]:
     * 1. Validação de tamanho máximo de 10MB.
     * 2. Verificação prévia de que o termo do cliente foi aceito antes do upload no bucket.
     * 3. Armazenamento no bucket configurado (Cloudinary em formato webp otimizado).
     *
     * @param file Arquivo da imagem recebido
     * @param agendamentoId ID do agendamento vinculado
     * @param tipo Tipo da foto (ex: FOTO_DEPOIS, FOTO_ANTES, REFERENCIA)
     * @return Entidade Midia salva no banco com URL segura do Cloudinary
     */
    public Midia uploadMidia(MultipartFile file, Long agendamentoId, String tipo) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("O arquivo de imagem é obrigatório e não pode estar vazio.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("Arquivo excede o tamanho máximo permitido de 10MB.");
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank() && !contentType.startsWith("image/")) {
            throw new BusinessException("Formato de arquivo inválido. Apenas imagens são permitidas.");
        }

        if (agendamentoId == null) {
            throw new BusinessException("agendamentoId é obrigatório para envio de mídia.");
        }

        if (agendamentoRepository != null && !agendamentoRepository.existsById(agendamentoId)) {
            throw new ResourceNotFoundException("Agendamento não encontrado com o ID: " + agendamentoId);
        }

        // Validação da Fase 4 / F3-BACK-02: Bloqueio rigoroso de upload no bucket sem termo aceito
        validarAceitePrevio(agendamentoId);

        if (imagemService == null) {
            throw new BusinessException("Serviço de upload de imagens não configurado.");
        }

        // Armazenamento no bucket configurado (Cloudinary)
        String secureUrl = imagemService.uploadImagem(file);

        String tipoFinal = (tipo != null && !tipo.isBlank()) ? tipo.trim() : "FOTO_DEPOIS";

        Midia midia = Midia.builder()
                .agendamentoId(agendamentoId)
                .url(secureUrl)
                .tipo(tipoFinal)
                .dataUpload(LocalDateTime.now())
                .build();

        return midiaRepository.save(midia);
    }

    /**
     * Gera legenda para publicação no Instagram através da Gemini API com conformidade estrita à RN03 [F4-BACK-02, UC03.1].
     *
     * Critérios de aceite:
     * - Chamada à Gemini API com contexto da imagem/estilo
     * - Legenda obrigatoriamente contém link do WhatsApp, estilo e localização (RN03)
     * - Fallback robusto caso a IA fique indisponível
     *
     * @param request Dados da requisição
     * @return LegendaResponse formatada
     */
    public LegendaResponse gerarLegenda(GerarLegendaRequest request) {
        if (request == null) {
            request = new GerarLegendaRequest();
        }

        // Se midiaId informado, enriquece o contexto a partir do banco
        if (request.getMidiaId() != null) {
            var midiaOpt = midiaRepository.findById(request.getMidiaId());
            if (midiaOpt.isPresent()) {
                Midia midia = midiaOpt.get();
                if (request.getImageUrl() == null || request.getImageUrl().isBlank()) {
                    request.setImageUrl(midia.getUrl());
                }
                if (request.getAgendamentoId() == null) {
                    request.setAgendamentoId(midia.getAgendamentoId());
                }
            }
        }

        // Se agendamentoId informado, enriquece com dados da sessão
        if (request.getAgendamentoId() != null && agendamentoRepository != null) {
            var agendamentoOpt = agendamentoRepository.findById(request.getAgendamentoId());
            if (agendamentoOpt.isPresent()) {
                var agendamento = agendamentoOpt.get();
                if ((request.getDescricao() == null || request.getDescricao().isBlank()) && agendamento.getDescricaoSessao() != null) {
                    request.setDescricao(agendamento.getDescricaoSessao());
                }
            }
        }

        if (geminiService == null) {
            throw new BusinessException("Serviço de IA Gemini não configurado.");
        }

        return geminiService.gerarLegenda(request);
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

    public void validarAceitePrevio(Long agendamentoId) {
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
