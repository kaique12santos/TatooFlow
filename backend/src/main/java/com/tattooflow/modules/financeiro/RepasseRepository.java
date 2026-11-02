package com.tattooflow.modules.financeiro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepasseRepository extends JpaRepository<Repasse, Long> {

    List<Repasse> findByTatuadorId(Long tatuadorId);

    List<Repasse> findByAgendamentoId(Long agendamentoId);

    boolean existsByAgendamentoId(Long agendamentoId);

    Optional<Repasse> findFirstByAgendamentoIdOrderByDataRepasseDesc(Long agendamentoId);
}
