package com.tattooflow.modules.notificacao;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificacaoScheduler {

    private final NotificacaoService notificacaoService;

    public NotificacaoScheduler(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @Scheduled(cron = "0 0 9 * * ?") // Todos os dias às 09:00
    public void agendarNotificacoesDiarias() {
        // Envio automático de lembretes
    }
}
