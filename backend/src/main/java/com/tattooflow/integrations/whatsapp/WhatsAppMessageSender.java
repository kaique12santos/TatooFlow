package com.tattooflow.integrations.whatsapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
        if (properties.isEnabled()) {
            client.enviarMensagem(telefone, texto);
        }
        registrarEnvio(telefone, texto);
    }

    public void enviarTemplate(String telefone, String nome, String idioma,
                               List<String> parametros, String textoSimulado) {
        if (properties.isEnabled()) {
            client.enviarTemplate(telefone, nome, idioma, parametros);
        } else if (!properties.getMock().isEnabled()) {
            throw new IllegalStateException("Envio de cuidados exige WhatsApp habilitado ou mock explícito");
        }
        registrarEnvio(telefone, textoSimulado);
    }

    private void registrarEnvio(String telefone, String texto) {
        historicoEnvios.add(new MensagemEnviada(telefone, texto));
        log.info("[WHATSAPP {}] Envio registrado", properties.isEnabled() ? "OFICIAL" : "MOCK/DEV");
    }

    public List<MensagemEnviada> getHistoricoEnvios() {
        return Collections.unmodifiableList(historicoEnvios);
    }

    public void limparHistorico() {
        historicoEnvios.clear();
    }
}
