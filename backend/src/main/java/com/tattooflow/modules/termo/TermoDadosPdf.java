package com.tattooflow.modules.termo;

import com.tattooflow.modules.agendamento.Agendamento;
import com.tattooflow.modules.cliente.Cliente;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TermoDadosPdf(
        Long termoId,
        String nomeCliente,
        String telefoneCliente,
        String emailCliente,
        String cpfCliente,
        String historicoSaude,
        Long agendamentoId,
        LocalDateTime dataHoraAgendamento,
        Integer duracaoMinutos,
        String descricaoSessao,
        LocalDateTime dataAceite,
        String ipAceite,
        LocalDateTime dataExpiracaoRetencao,
        String protocolo,
        String hashAutenticidade
) {
    public static TermoDadosPdf de(TermoAceite termo, Cliente cliente, Agendamento agendamento) {
        return TermoDadosPdf.builder()
                .termoId(termo != null ? termo.getId() : null)
                .nomeCliente(cliente != null ? cliente.getNome() : "Cliente não identificado")
                .telefoneCliente(cliente != null ? cliente.getTelefone() : null)
                .emailCliente(cliente != null ? cliente.getEmail() : null)
                .historicoSaude(cliente != null ? cliente.getHistoricoSaude() : null)
                .agendamentoId(agendamento != null ? agendamento.getId() : (termo != null ? termo.getAgendamentoId() : null))
                .dataHoraAgendamento(agendamento != null ? agendamento.getDataHora() : null)
                .duracaoMinutos(agendamento != null ? agendamento.getDuracaoMinutos() : null)
                .descricaoSessao(agendamento != null ? agendamento.getDescricaoSessao() : null)
                .dataAceite(termo != null && termo.getDataAssinatura() != null ? termo.getDataAssinatura() : LocalDateTime.now())
                .ipAceite(termo != null ? termo.getIpAssinatura() : "127.0.0.1")
                .dataExpiracaoRetencao(termo != null ? termo.getDataExpiracaoRetencao() : null)
                .protocolo(String.format("TF-TERMO-%06d", termo != null && termo.getId() != null ? termo.getId() : 0L))
                .build();
    }
}

