package com.tattooflow.modules.cliente;

import com.tattooflow.integrations.whatsapp.WhatsAppMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener que conecta eventos de mensagens recebidas pelo WhatsApp (F2-BACK-01)
 * com o serviço de fluxo de triagem do bot (F2-BACK-02).
 */
@Component
public class BotTriagemEventListener {

    private static final Logger log = LoggerFactory.getLogger(BotTriagemEventListener.class);
    private final BotTriagemService botTriagemService;

    public BotTriagemEventListener(BotTriagemService botTriagemService) {
        this.botTriagemService = botTriagemService;
    }

    @EventListener
    public void onWhatsAppMessageReceived(WhatsAppMessageReceivedEvent event) {
        log.info("[EVENTO BOT TRIAGEM] Recebida mensagem de {}: {}", event.telefone(), event.texto());
        botTriagemService.processarMensagem(event.telefone(), event.nomeContato(), event.texto());
    }
}
