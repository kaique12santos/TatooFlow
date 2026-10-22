package com.tattooflow.modules.notificacao;

import com.tattooflow.modules.agendamento.AgendamentoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.function.Consumer;

@Component
@ConditionalOnProperty(prefix = "notificacoes.cuidados", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NotificacaoScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoScheduler.class);
    private final AgendamentoRepository agendamentos;
    private final NotificacaoService service;
    private final NotificacaoProperties properties;
    private final Clock notificacaoClock;

    public NotificacaoScheduler(AgendamentoRepository agendamentos, NotificacaoService service,
                                NotificacaoProperties properties, Clock notificacaoClock) {
        this.agendamentos = agendamentos;
        this.service = service;
        this.properties = properties;
        this.notificacaoClock = notificacaoClock;
    }

    @Scheduled(fixedDelayString = "${notificacoes.cuidados.intervalo-ms:60000}",
            initialDelayString = "${notificacoes.cuidados.intervalo-ms:60000}")
    public void agendarNotificacoesPreSessao() {
        LocalDateTime agora = LocalDateTime.now(notificacaoClock);
        agendamentos.buscarPendentesPreSessao(agora, agora.plusHours(properties.getPreHoras()))
                .forEach(id -> tentarEnviar(id, TipoNotificacao.PRE_SESSAO, service::enviarNotificacaoCuidadosPreSessao));
    }

    @Scheduled(fixedDelayString = "${notificacoes.cuidados.intervalo-ms:60000}",
            initialDelayString = "${notificacoes.cuidados.intervalo-ms:60000}")
    public void agendarNotificacoesPosSessao() {
        LocalDateTime limite = LocalDateTime.now(notificacaoClock).minusHours(properties.getPosHoras());
        agendamentos.buscarPendentesPosSessao(limite)
                .forEach(id -> tentarEnviar(id, TipoNotificacao.POS_SESSAO, service::enviarNotificacaoCuidadosPosSessao));
    }

    private void tentarEnviar(Long id, TipoNotificacao tipo, Consumer<Long> enviar) {
        try {
            enviar.accept(id);
        } catch (RuntimeException ex) {
            // Não registra telefone, mensagem, credenciais ou corpo de erro do provedor.
            log.warn("Falha no envio {} do agendamento {}; nova tentativa no próximo ciclo ({})",
                    tipo, id, ex.getClass().getSimpleName());
        }
    }
}
