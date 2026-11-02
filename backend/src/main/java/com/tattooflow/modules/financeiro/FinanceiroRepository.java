package com.tattooflow.modules.financeiro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinanceiroRepository extends JpaRepository<Pagamento, Long> {
    Optional<Pagamento> findByAgendamentoIdAndTipo(Long agendamentoId, String tipo);

    boolean existsByAgendamentoIdAndTipo(Long agendamentoId, String tipo);

    List<Pagamento> findByAgendamentoId(Long agendamentoId);

    List<Pagamento> findByAgendamentoIdAndStatus(Long agendamentoId, String status);

    @Query("""
        SELECT p FROM Pagamento p
        WHERE p.dataPagamento >= :inicio AND p.dataPagamento <= :fim
        ORDER BY p.dataPagamento ASC, p.id ASC
    """)
    List<Pagamento> buscarPorPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    List<Pagamento> findByDataPagamentoBetween(LocalDateTime inicio, LocalDateTime fim);
}
