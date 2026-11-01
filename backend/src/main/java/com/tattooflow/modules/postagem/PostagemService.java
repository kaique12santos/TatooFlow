package com.tattooflow.modules.postagem;

import com.tattooflow.common.exception.BusinessException;
import com.tattooflow.common.exception.ResourceNotFoundException;
import com.tattooflow.modules.midia.Midia;
import com.tattooflow.modules.midia.MidiaRepository;
import com.tattooflow.service.InstagramService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço responsável pelo agendamento, orquestração e registro de logs de publicações no Instagram [F4-BACK-03, UC03.2].
 */
@Service
public class PostagemService {

    private static final Logger log = LoggerFactory.getLogger(PostagemService.class);

    private final PostagemAgendadaRepository postagemAgendadaRepository;
    private final PostagemLogRepository postagemLogRepository;
    private final InstagramService instagramService;
    private final MidiaRepository midiaRepository;

    @Autowired
    public PostagemService(PostagemAgendadaRepository postagemAgendadaRepository,
                           PostagemLogRepository postagemLogRepository,
                           InstagramService instagramService,
                           MidiaRepository midiaRepository) {
        this.postagemAgendadaRepository = postagemAgendadaRepository;
        this.postagemLogRepository = postagemLogRepository;
        this.instagramService = instagramService;
        this.midiaRepository = midiaRepository;
    }

    /**
     * Agenda uma postagem para publicação no Instagram Graph API [F4-BACK-03].
     *
     * @param request Dados da requisição
     * @return DTO com protocolo e dados do agendamento
     */
    @Transactional
    public PostagemAgendadaResponse agendarPostagem(AgendarPostagemRequest request) {
        if (request == null) {
            throw new BusinessException("Os dados de agendamento da postagem são obrigatórios.");
        }

        // 1. Extração e resolução da imagem
        String imageUrl = request.getImageUrl();
        if ((imageUrl == null || imageUrl.isBlank()) && request.getMidiaId() != null && midiaRepository != null) {
            Optional<Midia> midiaOpt = midiaRepository.findById(request.getMidiaId());
            if (midiaOpt.isPresent()) {
                imageUrl = midiaOpt.get().getUrl();
                if (request.getAgendamentoId() == null) {
                    request.setAgendamentoId(midiaOpt.get().getAgendamentoId());
                }
            }
        }

        if (imageUrl == null || imageUrl.isBlank()) {
            throw new BusinessException("A URL da imagem é obrigatória para agendar a postagem.");
        }

        // 2. Validação da legenda
        String legenda = request.getLegenda();
        if (legenda == null || legenda.isBlank()) {
            throw new BusinessException("A legenda é obrigatória para agendar a postagem.");
        }

        // 3. Determinação da data e horário programado
        LocalDateTime dataHoraAgendada = resolverDataHora(request);

        // 4. Geração de protocolo único de rastreabilidade
        String protocolo = gerarProtocolo();

        // 5. Extração de canais selecionados
        String canaisFormatados = extrairCanais(request.getChannels());

        // 6. Construção e persistência do agendamento
        PostagemAgendada postagem = PostagemAgendada.builder()
                .agendamentoId(request.getAgendamentoId())
                .midiaId(request.getMidiaId())
                .imageUrl(imageUrl)
                .legenda(legenda)
                .dataHoraAgendada(dataHoraAgendada)
                .status("AGENDADO")
                .protocolo(protocolo)
                .canais(canaisFormatados)
                .contaInstagram((request.getAccountId() != null) ? request.getAccountId() : instagramService.getAccountId())
                .tentativas(0)
                .createdAt(LocalDateTime.now())
                .build();

        postagem = postagemAgendadaRepository.save(postagem);

        // 7. Registro de log de auditoria (Critério de aceite 3)
        registrarLog(postagem.getId(), "AGENDAMENTO_CRIADO",
                "Postagem agendada para " + dataHoraAgendada + " com protocolo " + protocolo,
                null, true);

        log.info("Postagem agendada com sucesso. ID={}, Protocolo={}, DataHora={}",
                postagem.getId(), protocolo, dataHoraAgendada);

        return PostagemAgendadaResponse.fromEntity(postagem);
    }

    /**
     * Job agendado que varre postagens com status AGENDADO e horário vencido para publicação [Critério de aceite 2].
     *
     * @return Quantidade de postagens processadas
     */
    @Transactional
    public int processarPostagensPendentes() {
        LocalDateTime agora = LocalDateTime.now();
        List<PostagemAgendada> pendentes = postagemAgendadaRepository
                .findByStatusAndDataHoraAgendadaLessThanEqual("AGENDADO", agora);

        if (pendentes.isEmpty()) {
            return 0;
        }

        log.info("Iniciando processamento em lote de postagens pendentes. Quantidade encontrada: {}", pendentes.size());
        int processadas = 0;

        for (PostagemAgendada postagem : pendentes) {
            try {
                executarPublicacao(postagem);
                processadas++;
            } catch (Exception e) {
                log.error("Erro inesperado ao processar postagem agendada ID={}: {}", postagem.getId(), e.getMessage());
            }
        }

        log.info("Processamento de postagens concluído. Total processadas: {}", processadas);
        return processadas;
    }

    /**
     * Executa o disparo da publicação através do InstagramService e registra todos os logs de auditoria [Critérios 2 e 3].
     *
     * @param postagem Postagem agendada a ser publicada
     * @return Postagem atualizada com status e IDs do Instagram
     */
    @Transactional
    public PostagemAgendada executarPublicacao(PostagemAgendada postagem) {
        if (postagem == null) {
            return null;
        }

        log.info("Iniciando publicação da postagem [{}] no Instagram Graph API", postagem.getProtocolo());
        postagem.setStatus("PUBLICANDO");
        postagemAgendadaRepository.save(postagem);

        registrarLog(postagem.getId(), "DISPARO_INICIADO",
                "Disparo da publicação iniciado no horário " + LocalDateTime.now(), null, true);

        PublicacaoResultado resultado = instagramService.publicarFoto(postagem.getImageUrl(), postagem.getLegenda());

        if (resultado.isSucesso()) {
            postagem.setStatus("PUBLICADO");
            postagem.setInstagramContainerId(resultado.getContainerId());
            postagem.setInstagramMediaId(resultado.getMediaId());
            postagem.setDataHoraPublicacao(LocalDateTime.now());
            postagem.setMensagemErro(null);
            postagemAgendadaRepository.save(postagem);

            registrarLog(postagem.getId(), "PUBLICADO_SUCESSO",
                    "Publicação concluída com sucesso no feed do Instagram. MediaId: " + resultado.getMediaId(),
                    resultado.getRespostaJson(), true);

            log.info("Postagem [{}] publicada com sucesso! Instagram MediaId={}", postagem.getProtocolo(), resultado.getMediaId());
        } else {
            postagem.setTentativas(postagem.getTentativas() + 1);
            postagem.setStatus("ERRO");
            postagem.setMensagemErro(resultado.getMensagemErro());
            postagemAgendadaRepository.save(postagem);

            registrarLog(postagem.getId(), "FALHA_PUBLICACAO",
                    "Falha ao publicar no Instagram: " + resultado.getMensagemErro(),
                    resultado.getRespostaJson(), false);

            log.error("Falha ao publicar postagem [{}]: {}", postagem.getProtocolo(), resultado.getMensagemErro());
        }

        return postagem;
    }

    /**
     * Registra evento na tabela de auditoria de logs [Critério 3].
     */
    @Transactional
    public void registrarLog(Long postagemId, String tipoEvento, String mensagem, String detalhesJson, boolean sucesso) {
        try {
            PostagemLog logEntity = PostagemLog.builder()
                    .postagemAgendadaId(postagemId)
                    .tipoEvento(tipoEvento)
                    .mensagem(mensagem)
                    .detalhesJson(detalhesJson)
                    .sucesso(sucesso)
                    .dataHora(LocalDateTime.now())
                    .build();

            postagemLogRepository.save(logEntity);
        } catch (Exception e) {
            log.error("Erro ao registrar log de postagem no banco: {}", e.getMessage());
        }
    }

    public List<PostagemLog> listarLogs(Long postagemId) {
        return postagemLogRepository.findByPostagemAgendadaIdOrderByDataHoraDesc(postagemId);
    }

    public List<PostagemAgendada> listarTodas() {
        return postagemAgendadaRepository.findByOrderByDataHoraAgendadaDesc();
    }

    public PostagemAgendada buscarPorId(Long id) {
        return postagemAgendadaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Postagem não encontrada com o ID: " + id));
    }

    public PostagemAgendada buscarPorProtocolo(String protocolo) {
        return postagemAgendadaRepository.findByProtocolo(protocolo)
                .orElseThrow(() -> new ResourceNotFoundException("Postagem não encontrada com o protocolo: " + protocolo));
    }

    // =========================================================================
    // Métodos Utilitários Internos
    // =========================================================================

    private LocalDateTime resolverDataHora(AgendarPostagemRequest request) {
        if (request.getDataHoraAgendada() != null) {
            return request.getDataHoraAgendada();
        }

        String dataStr = request.getScheduleDate();
        String horaStr = request.getScheduleTime();

        if (dataStr == null || dataStr.isBlank()) {
            return LocalDateTime.now();
        }

        try {
            LocalDate date = parseData(dataStr.trim());
            LocalTime time = (horaStr != null && !horaStr.isBlank())
                    ? LocalTime.parse(horaStr.trim(), DateTimeFormatter.ofPattern("HH:mm"))
                    : LocalTime.of(12, 0);

            return LocalDateTime.of(date, time);
        } catch (Exception e) {
            log.warn("Formato de data inválido ({}, {}). Utilizando data e hora atual como padrão: {}",
                    dataStr, horaStr, e.getMessage());
            return LocalDateTime.now();
        }
    }

    private LocalDate parseData(String dataStr) {
        // Suporte a "23/MAI/2025" ou "24/05/2025" ou "2026-11-25"
        String limpa = dataStr.toUpperCase();
        Map<String, String> meses = Map.ofEntries(
                Map.entry("JAN", "01"), Map.entry("FEV", "02"), Map.entry("MAR", "03"),
                Map.entry("ABR", "04"), Map.entry("MAI", "05"), Map.entry("JUN", "06"),
                Map.entry("JUL", "07"), Map.entry("AGO", "08"), Map.entry("SET", "09"),
                Map.entry("OUT", "10"), Map.entry("NOV", "11"), Map.entry("DEZ", "12")
        );

        for (var entry : meses.entrySet()) {
            if (limpa.contains(entry.getKey())) {
                limpa = limpa.replace(entry.getKey(), entry.getValue());
                break;
            }
        }

        if (limpa.matches("\\d{2}/\\d{2}/\\d{4}")) {
            return LocalDate.parse(limpa, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } else if (limpa.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return LocalDate.parse(limpa, DateTimeFormatter.ISO_LOCAL_DATE);
        }

        return LocalDate.now();
    }

    private String extrairCanais(Map<String, Boolean> channels) {
        if (channels == null || channels.isEmpty()) {
            return "FEED";
        }
        return channels.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(e -> e.getKey().toUpperCase())
                .collect(Collectors.joining(","));
    }

    private String gerarProtocolo() {
        String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sufixo = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "TATTOO-MKT-" + data + "-" + sufixo;
    }
}

