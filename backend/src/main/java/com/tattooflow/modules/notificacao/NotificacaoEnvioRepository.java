package com.tattooflow.modules.notificacao;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacaoEnvioRepository extends JpaRepository<NotificacaoEnvio, Long> {
    boolean existsByAgendamentoIdAndTipo(Long agendamentoId, TipoNotificacao tipo);
}
