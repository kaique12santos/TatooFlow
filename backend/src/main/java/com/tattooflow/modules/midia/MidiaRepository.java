package com.tattooflow.modules.midia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MidiaRepository extends JpaRepository<Midia, Long> {
    List<Midia> findByAgendamentoId(Long agendamentoId);
}
