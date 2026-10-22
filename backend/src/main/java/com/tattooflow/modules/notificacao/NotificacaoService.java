package com.tattooflow.modules.notificacao;

import com.tattooflow.integrations.whatsapp.WhatsAppMessageSender;
import com.tattooflow.modules.agendamento.Agendamento;
import com.tattooflow.modules.agendamento.AgendamentoRepository;
import com.tattooflow.modules.cliente.Cliente;
import com.tattooflow.modules.cliente.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificacaoService {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final AgendamentoRepository agendamentos;
    private final ClienteRepository clientes;
    private final NotificacaoEnvioRepository envios;
    private final WhatsAppMessageSender sender;
    private final NotificacaoProperties properties;
    private final Clock notificacaoClock;

    public NotificacaoService(AgendamentoRepository agendamentos, ClienteRepository clientes,
                              NotificacaoEnvioRepository envios, WhatsAppMessageSender sender,
                              NotificacaoProperties properties, Clock notificacaoClock) {
        this.agendamentos = agendamentos;
        this.clientes = clientes;
        this.envios = envios;
        this.sender = sender;
        this.properties = properties;
        this.notificacaoClock = notificacaoClock;
    }

    // Cada envio tem sua própria transação: uma falha não desfaz os outros envios do job.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enviarNotificacaoCuidadosPreSessao(Long agendamentoId) {
        enviar(agendamentoId, TipoNotificacao.PRE_SESSAO);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enviarNotificacaoCuidadosPosSessao(Long agendamentoId) {
        enviar(agendamentoId, TipoNotificacao.POS_SESSAO);
    }

    private void enviar(Long agendamentoId, TipoNotificacao tipo) {
        // Serializa os jobs e eventuais instâncias concorrentes para este agendamento.
        Agendamento agendamento = agendamentos.buscarParaNotificacao(agendamentoId).orElse(null);
        LocalDateTime agora = LocalDateTime.now(notificacaoClock);
        if (agendamento == null || !deveEnviar(agendamento, tipo, agora)
                || envios.existsByAgendamentoIdAndTipo(agendamentoId, tipo)) {
            return;
        }

        Cliente cliente = clientes.findById(agendamento.getClienteId())
                .orElseThrow(() -> new IllegalStateException("Cliente do agendamento não encontrado"));
        if (!StringUtils.hasText(cliente.getTelefone())) {
            throw new IllegalStateException("Cliente do agendamento sem telefone");
        }

        String template = tipo == TipoNotificacao.PRE_SESSAO
                ? properties.getTemplatePre() : properties.getTemplatePos();
        String templateWhatsApp = tipo == TipoNotificacao.PRE_SESSAO
                ? properties.getWhatsappTemplatePre() : properties.getWhatsappTemplatePos();
        List<String> parametros = List.of(cliente.getNome(), DATA.format(agendamento.getDataHora()),
                HORA.format(agendamento.getDataHora()));
        String texto = template.replace("{nome}", parametros.get(0))
                .replace("{data}", parametros.get(1)).replace("{hora}", parametros.get(2));

        sender.enviarTemplate(cliente.getTelefone(), templateWhatsApp, properties.getWhatsappIdioma(),
                parametros, texto);
        envios.saveAndFlush(NotificacaoEnvio.builder()
                .agendamentoId(agendamentoId).tipo(tipo).enviadoEm(agora).build());
    }

    private boolean deveEnviar(Agendamento agendamento, TipoNotificacao tipo, LocalDateTime agora) {
        if (tipo == TipoNotificacao.PRE_SESSAO) {
            return "AGENDADO".equals(agendamento.getStatus())
                    && agendamento.getDataHora().isAfter(agora)
                    && !agendamento.getDataHora().minusHours(properties.getPreHoras()).isAfter(agora);
        }
        return "CONCLUIDO".equals(agendamento.getStatus())
                && !agendamento.getDataHora().plusMinutes(agendamento.getDuracaoMinutos())
                        .plusHours(properties.getPosHoras()).isAfter(agora);
    }
}
