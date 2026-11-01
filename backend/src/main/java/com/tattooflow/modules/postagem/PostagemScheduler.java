package com.tattooflow.modules.postagem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Job agendado responsável por verificar e disparar publicações no horário programado [F4-BACK-03, Critério 2].
 */
@Component
public class PostagemScheduler {

    private static final Logger log = LoggerFactory.getLogger(PostagemScheduler.class);

    private final PostagemService postagemService;

    @Value("${instagram.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    public PostagemScheduler(PostagemService postagemService) {
        this.postagemService = postagemService;
    }

    /**
     * Executa periodicamente a busca e disparo das publicações cujo horário já foi atingido.
     */
    @Scheduled(fixedDelayString = "${instagram.scheduler.intervalo-ms:30000}", initialDelay = 5000)
    public void dispararPublicacoesAgendadas() {
        if (!schedulerEnabled) {
            return;
        }

        try {
            int processadas = postagemService.processarPostagensPendentes();
            if (processadas > 0) {
                log.info("[PostagemScheduler] {} postagens foram processadas e enviadas ao Instagram.", processadas);
            }
        } catch (Exception e) {
            log.error("[PostagemScheduler] Erro durante o processamento do job de publicações: {}", e.getMessage(), e);
        }
    }
}

