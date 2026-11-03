package com.tattooflow.modules.financeiro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepasseRepository extends JpaRepository<Repasse, Long> {

    List<Repasse> findByTatuadorId(Long tatuadorId);

    List<Repasse> findByAgendamentoId(Long agendamentoId);

    boolean existsByAgendamentoId(Long agendamentoId);

    Optional<Repasse> findFirstByAgendamentoIdOrderByDataRepasseDesc(Long agendamentoId);

    @Query("""
        SELECT r FROM Repasse r
        WHERE r.dataRepasse >= :inicio AND r.dataRepasse <= :fim
        ORDER BY r.dataRepasse ASC, r.id ASC
    """)
    List<Repasse> buscarPorPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    List<Repasse> findByDataRepasseBetween(LocalDateTime inicio, LocalDateTime fim);
}
