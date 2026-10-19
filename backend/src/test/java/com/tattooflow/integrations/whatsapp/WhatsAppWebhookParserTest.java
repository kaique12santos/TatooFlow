package com.tattooflow.integrations.whatsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WhatsAppWebhookParserTest {

    private WhatsAppWebhookParser parser;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        parser = new WhatsAppWebhookParser(objectMapper);
    }

    @Test
    void deveFazerParseDeMensagemDeTextoPadraoMetaComSucesso() {
        String json = """
                {
                  "object": "whatsapp_business_account",
                  "entry": [
                    {
                      "id": "123456789",
                      "changes": [
                        {
                          "value": {
                            "messaging_product": "whatsapp",
                            "metadata": {
                              "display_phone_number": "5511988888888",
                              "phone_number_id": "phone-123"
                            },
                            "contacts": [
                              {
                                "profile": {
                                  "name": "Maria Silva"
                                },
                                "wa_id": "5511999991111"
                              }
                            ],
                            "messages": [
                              {
                                "from": "5511999991111",
                                "id": "wamid.HBgLMTE5OTk5OTE",
                                "timestamp": "1729000000",
                                "type": "text",
                                "text": {
                                  "body": "Ola, gostaria de agendar uma tattoo de borboleta"
                                }
                              }
                            ]
                          },
                          "field": "messages"
                        }
                      ]
                    }
                  ]
                }
                """;

        List<WhatsAppMessageReceivedEvent> events = parser.parse(json.getBytes(StandardCharsets.UTF_8));

        assertThat(events).hasSize(1);
        WhatsAppMessageReceivedEvent event = events.get(0);
        assertThat(event.source()).isEqualTo("META");
        assertThat(event.messageId()).isEqualTo("wamid.HBgLMTE5OTk5OTE");
        assertThat(event.telefone()).isEqualTo("5511999991111");
        assertThat(event.nomeContato()).isEqualTo("Maria Silva");
        assertThat(event.texto()).isEqualTo("Ola, gostaria de agendar uma tattoo de borboleta");
        assertThat(event.tipo()).isEqualTo("text");
        assertThat(event.receivedAt().getEpochSecond()).isEqualTo(1729000000L);
    }

    @Test
    void deveRetornarNomePadraoQuandoContatoNaoInformado() {
        String json = """
                {
                  "object": "whatsapp_business_account",
                  "entry": [
                    {
                      "id": "123456789",
                      "changes": [
                        {
                          "value": {
                            "messaging_product": "whatsapp",
                            "messages": [
                              {
                                "from": "5511999992222",
                                "id": "wamid.123456",
                                "timestamp": "1729000100",
                                "type": "text",
                                "text": {
                                  "body": "Tatuagem no braco"
                                }
                              }
                            ]
                          },
                          "field": "messages"
                        }
                      ]
                    }
                  ]
                }
                """;

        List<WhatsAppMessageReceivedEvent> events = parser.parse(json.getBytes(StandardCharsets.UTF_8));

        assertThat(events).hasSize(1);
        WhatsAppMessageReceivedEvent event = events.get(0);
        assertThat(event.telefone()).isEqualTo("5511999992222");
        assertThat(event.nomeContato()).isEqualTo("Cliente WhatsApp");
        assertThat(event.texto()).isEqualTo("Tatuagem no braco");
    }

    @Test
    void deveRetornarListaVaziaParaPingOuStatusSemMensagens() {
        String jsonPing = "{\"object\":\"whatsapp_business_account\",\"entry\":[]}";
        List<WhatsAppMessageReceivedEvent> events = parser.parse(jsonPing.getBytes(StandardCharsets.UTF_8));
        assertThat(events).isEmpty();

        String jsonSemEntry = "{\"object\":\"whatsapp_business_account\"}";
        List<WhatsAppMessageReceivedEvent> eventsSemEntry = parser.parse(jsonSemEntry.getBytes(StandardCharsets.UTF_8));
        assertThat(eventsSemEntry).isEmpty();
    }

    @Test
    void deveRetornarListaVaziaParaPayloadInvalidoOuNulo() {
        assertThat(parser.parse((byte[]) null)).isEmpty();
        assertThat(parser.parse(new byte[0])).isEmpty();
        assertThat(parser.parse("invalido".getBytes(StandardCharsets.UTF_8))).isEmpty();
    }
}
