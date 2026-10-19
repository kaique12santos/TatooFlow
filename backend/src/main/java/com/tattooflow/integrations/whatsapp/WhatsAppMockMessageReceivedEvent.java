package com.tattooflow.integrations.whatsapp;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

/** Evento local de texto recebido. Nao representa entrega ou processamento pela Meta. */
public record WhatsAppMockMessageReceivedEvent(
        String source,
        String messageId,
        String telefone,
        String texto,
        Instant receivedAt,
        JsonNode payload) {
}
