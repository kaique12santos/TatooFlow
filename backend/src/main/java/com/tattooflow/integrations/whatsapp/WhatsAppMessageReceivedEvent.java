package com.tattooflow.integrations.whatsapp;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

/**
 * Evento de mensagem recebida via WhatsApp (oficial da Meta ou mock local).
 * Utilizado pelos consumidores de triagem e bot (F2-BACK-02).
 */
public record WhatsAppMessageReceivedEvent(
        String source,
        String messageId,
        String telefone,
        String nomeContato,
        String texto,
        String tipo,
        Instant receivedAt,
        JsonNode payload) {
}
