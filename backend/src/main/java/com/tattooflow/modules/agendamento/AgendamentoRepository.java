package com.tattooflow.modules.agendamento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    List<Agendamento> findByTatuadorId(Long tatuadorId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Agendamento a where a.id = :id")
    Optional<Agendamento> buscarPorIdParaAtualizacao(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Agendamento a WHERE a.id = :id")
    Optional<Agendamento> buscarParaNotificacao(@Param("id") Long id);

    @Query("""
        SELECT a.id FROM Agendamento a
        WHERE a.status IN ('AGENDADO', 'CONFIRMADO') AND a.dataHora > :agora AND a.dataHora <= :limite
          AND NOT EXISTS (SELECT n.id FROM NotificacaoEnvio n
                          WHERE n.agendamentoId = a.id
                            AND n.tipo = com.tattooflow.modules.notificacao.TipoNotificacao.PRE_SESSAO)
        ORDER BY a.dataHora, a.id
        """)
    List<Long> buscarPendentesPreSessao(@Param("agora") LocalDateTime agora,
                                      @Param("limite") LocalDateTime limite);

    // O término (início + duração) é revalidado pelo serviço antes do envio.
    @Query("""
        SELECT a.id FROM Agendamento a
        WHERE a.status = 'CONCLUIDO' AND a.dataHora <= :limite
          AND NOT EXISTS (SELECT n.id FROM NotificacaoEnvio n
                          WHERE n.agendamentoId = a.id
                            AND n.tipo = com.tattooflow.modules.notificacao.TipoNotificacao.POS_SESSAO)
        ORDER BY a.dataHora, a.id
        """)
    List<Long> buscarPendentesPosSessao(@Param("limite") LocalDateTime limite);

    @Query("""
        SELECT a FROM Agendamento a
        WHERE a.tatuadorId = :tatuadorId
          AND (a.status IS NULL OR a.status <> 'CANCELADO')
          AND a.dataHora < :fim
          AND a.dataHora > :inicioJanela
        """)
    List<Agendamento> buscarCandidatosConflito(@Param("tatuadorId") Long tatuadorId,
                                               @Param("inicioJanela") LocalDateTime inicioJanela,
                                               @Param("fim") LocalDateTime fim);
}
