package com.tattooflow.modules.postagem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório de logs de publicação no Instagram [F4-BACK-03].
 */
@Repository
public interface PostagemLogRepository extends JpaRepository<PostagemLog, Long> {

    List<PostagemLog> findByPostagemAgendadaIdOrderByDataHoraDesc(Long postagemAgendadaId);

    List<PostagemLog> findByOrderByDataHoraDesc();
}

