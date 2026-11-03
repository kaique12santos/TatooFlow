package com.tattooflow.integrations.whatsapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class WhatsAppMessageSenderTest {
    private WhatsAppProperties properties;
    private WhatsAppClient client;
    private WhatsAppMessageSender sender;

    @BeforeEach
    void preparar() {
        properties = new WhatsAppProperties();
        client = mock(WhatsAppClient.class);
        sender = new WhatsAppMessageSender(properties, client);
    }

    @Test
    void deveSimularTemplateSomenteComMockExplicito() {
        properties.getMock().setEnabled(true);
        enviar();
        verifyNoInteractions(client);
        assertThat(sender.getHistoricoEnvios()).containsExactly(
                new WhatsAppMessageSender.MensagemEnviada("5511999999999", "Olá, Ana!"));
    }

    @Test
    void semIntegracaoOuMockNaoDeveRegistrarSucessoFalso() {
        assertThatThrownBy(this::enviar).isInstanceOf(IllegalStateException.class);
        assertThat(sender.getHistoricoEnvios()).isEmpty();
        verifyNoInteractions(client);
    }

    @Test
    void deveEnviarTemplateComIntegracaoHabilitada() {
        properties.setEnabled(true);
        enviar();
        verify(client).enviarTemplate("5511999999999", "pre", "pt_BR", List.of("Ana"));
        assertThat(sender.getHistoricoEnvios()).hasSize(1);
    }

    @Test
    void falhaDoProvedorNaoDeveEntrarNoHistoricoDeSucessos() {
        properties.setEnabled(true);
        doThrow(new IllegalStateException("falha")).when(client)
                .enviarTemplate("5511999999999", "pre", "pt_BR", List.of("Ana"));
        assertThatThrownBy(this::enviar).isInstanceOf(IllegalStateException.class);
        assertThat(sender.getHistoricoEnvios()).isEmpty();
    }

    private void enviar() {
        sender.enviarTemplate("5511999999999", "pre", "pt_BR", List.of("Ana"), "Olá, Ana!");
    }
}
