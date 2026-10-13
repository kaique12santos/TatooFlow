package com.tattooflow.integrations.whatsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Profile("!prod & (dev | h2)")
@ConditionalOnProperty(prefix = "integrations.whatsapp.mock", name = "enabled", havingValue = "true")
public class WhatsAppMockService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppMockService.class);
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    public WhatsAppMockService(ObjectMapper objectMapper, ApplicationEventPublisher eventPublisher) {
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    public WhatsAppMockMessageReceivedEvent simularRecebimentoMensagem(SimularMensagemRequest request) {
        String messageId = "wamid.mock." + UUID.randomUUID();
        Instant receivedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("object", "whatsapp_business_account");
        ObjectNode entry = payload.putArray("entry").addObject();
        entry.put("id", "mock-waba-id");
        ObjectNode change = entry.putArray("changes").addObject();
        change.put("field", "messages");
        ObjectNode value = change.putObject("value");
        value.put("messaging_product", "whatsapp");
        value.putObject("metadata")
                .put("display_phone_number", "5500000000000")
                .put("phone_number_id", "mock-phone-number-id");
        ObjectNode contact = value.putArray("contacts").addObject();
        contact.put("wa_id", request.telefone());
        contact.putObject("profile").put("name", "Cliente de teste");
        ObjectNode message = value.putArray("messages").addObject();
        message.put("from", request.telefone());
        message.put("id", messageId);
        message.put("timestamp", Long.toString(receivedAt.getEpochSecond()));
        message.put("type", "text");
        message.putObject("text").put("body", request.texto());

        WhatsAppMockMessageReceivedEvent event = new WhatsAppMockMessageReceivedEvent(
                "MOCK", messageId, request.telefone(), request.texto(), receivedAt, payload);
        eventPublisher.publishEvent(event);
        log.info("[MOCK WHATSAPP] Mensagem recebida em simulacao local: {}", messageId);
        return event;
    }

    public void simularEnvioMensagem(String telefone, String texto) {
        log.info("[MOCK WHATSAPP] Envio simulado localmente; nenhuma chamada a Meta realizada");
    }
}
