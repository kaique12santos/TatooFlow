package com.tattooflow.modules.notificacao;

import org.springframework.stereotype.Service;

@Service
public class NotificacaoService {

    public void enviarNotificacaoCuidadosPreSessao(Long clienteId) {
        // Envio de lembrete de cuidados pré-sessão via WhatsApp/Email
    }

    public void enviarNotificacaoCuidadosPosSessao(Long clienteId) {
        // Envio de orientações de cicatrization pós-sessão
    }
}
