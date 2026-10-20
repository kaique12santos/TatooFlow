package com.tattooflow.modules.financeiro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FinanceiroRepository extends JpaRepository<Pagamento, Long> {
    Optional<Pagamento> findByAgendamentoIdAndTipo(Long agendamentoId, String tipo);

    boolean existsByAgendamentoIdAndTipo(Long agendamentoId, String tipo);
}
