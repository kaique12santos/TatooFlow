package com.tattooflow.modules.postagem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositório de postagens agendadas [F4-BACK-03].
 */
@Repository
public interface PostagemAgendadaRepository extends JpaRepository<PostagemAgendada, Long> {

    List<PostagemAgendada> findByStatusAndDataHoraAgendadaLessThanEqual(String status, LocalDateTime dataHora);

    Optional<PostagemAgendada> findByProtocolo(String protocolo);

    List<PostagemAgendada> findByAgendamentoId(Long agendamentoId);

    List<PostagemAgendada> findByOrderByDataHoraAgendadaDesc();
}

