package com.tattooflow.integrations.whatsapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Componente responsavel pelo parsing dos payloads JSON recebidos via webhook do WhatsApp Business API (Meta).
 * Extrai informacoes da mensagem (remetente, texto, ID, data/hora) e contatos.
 */
@Component
public class WhatsAppWebhookParser {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppWebhookParser.class);
    private final ObjectMapper objectMapper;

    public WhatsAppWebhookParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<WhatsAppMessageReceivedEvent> parse(byte[] payloadBytes) {
        if (payloadBytes == null || payloadBytes.length == 0) {
            return Collections.emptyList();
        }

        try {
            JsonNode root = objectMapper.readTree(payloadBytes);
            return parse(root);
        } catch (Exception e) {
            log.warn("Erro ao desserializar JSON do webhook WhatsApp: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<WhatsAppMessageReceivedEvent> parse(JsonNode root) {
        if (root == null || !root.has("entry") || !root.get("entry").isArray()) {
            return Collections.emptyList();
        }

        List<WhatsAppMessageReceivedEvent> events = new ArrayList<>();

        for (JsonNode entry : root.get("entry")) {
            JsonNode changes = entry.get("changes");
            if (changes == null || !changes.isArray()) {
                continue;
            }

            for (JsonNode change : changes) {
                JsonNode value = change.get("value");
                if (value == null) {
                    continue;
                }

                // Extrai mapa de contatos para resolver o nome do perfil pelo wa_id
                Map<String, String> contactNames = new HashMap<>();
                JsonNode contacts = value.get("contacts");
                if (contacts != null && contacts.isArray()) {
                    for (JsonNode contact : contacts) {
                        String waId = contact.path("wa_id").asText(null);
                        String profileName = contact.path("profile").path("name").asText(null);
                        if (waId != null && profileName != null && !profileName.isBlank()) {
                            contactNames.put(waId, profileName);
                        }
                    }
                }

                JsonNode messages = value.get("messages");
                if (messages == null || !messages.isArray()) {
                    continue;
                }

                for (JsonNode message : messages) {
                    String messageId = message.path("id").asText(null);
                    String from = message.path("from").asText(null);
                    String type = message.path("type").asText("text");
                    String timestampStr = message.path("timestamp").asText(null);

                    Instant receivedAt;
                    try {
                        if (timestampStr != null && !timestampStr.isBlank()) {
                            receivedAt = Instant.ofEpochSecond(Long.parseLong(timestampStr));
                        } else {
                            receivedAt = Instant.now();
                        }
                    } catch (Exception e) {
                        receivedAt = Instant.now();
                    }

                    String text = null;
                    if ("text".equalsIgnoreCase(type) && message.has("text")) {
                        text = message.get("text").path("body").asText(null);
                    } else if (message.has("text")) {
                        text = message.get("text").path("body").asText(null);
                    }

                    String contactName = contactNames.getOrDefault(from, "Cliente WhatsApp");

                    if (messageId != null && from != null) {
                        WhatsAppMessageReceivedEvent event = new WhatsAppMessageReceivedEvent(
                                "META",
                                messageId,
                                from,
                                contactName,
                                text,
                                type,
                                receivedAt,
                                root
                        );
                        events.add(event);
                        log.info("Mensagem WhatsApp recebida e parseada: ID={}, From='{}', Nome='{}', Texto='{}'",
                                messageId, from, contactName, text);
                    }
                }
            }
        }

        return events;
    }
}
