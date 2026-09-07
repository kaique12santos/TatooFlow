package com.tattooflow.modules.financeiro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinanceiroRepository extends JpaRepository<Pagamento, Long> {
}
