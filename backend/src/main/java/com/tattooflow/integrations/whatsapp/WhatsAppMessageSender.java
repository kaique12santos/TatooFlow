package com.tattooflow.integrations.whatsapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class WhatsAppMessageSender {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppMessageSender.class);
    private final WhatsAppProperties properties;
    private final WhatsAppClient client;
    private final List<MensagemEnviada> historicoEnvios = new CopyOnWriteArrayList<>();

    public record MensagemEnviada(String telefone, String texto) {}

    public WhatsAppMessageSender(WhatsAppProperties properties, WhatsAppClient client) {
        this.properties = properties;
        this.client = client;
    }

    public void enviarMensagem(String telefone, String texto) {
        historicoEnvios.add(new MensagemEnviada(telefone, texto));
        if (properties.isEnabled()) {
            log.info("[WHATSAPP OFICIAL] Enviando mensagem para {}: {}", telefone, texto);
            client.enviarMensagem(telefone, texto);
        } else {
            log.info("[WHATSAPP MOCK/DEV] Mensagem para {}: {}", telefone, texto);
        }
    }

    public List<MensagemEnviada> getHistoricoEnvios() {
        return Collections.unmodifiableList(historicoEnvios);
    }

    public void limparHistorico() {
        historicoEnvios.clear();
    }
}
