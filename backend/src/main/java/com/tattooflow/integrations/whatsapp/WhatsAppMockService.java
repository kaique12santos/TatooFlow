package com.tattooflow.integrations.whatsapp;

import org.springframework.stereotype.Service;

@Service
public class WhatsAppMockService {

    public void simularEnvioMensagem(String telefone, String texto) {
        System.out.println("[MOCK WHATSAPP] Enviado para " + telefone + ": " + texto);
    }
}
