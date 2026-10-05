package com.tattooflow.modules.agendamento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    List<Agendamento> findByTatuadorId(Long tatuadorId);

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