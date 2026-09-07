package com.tattooflow.modules.termo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TermoRepository extends JpaRepository<TermoAceite, Long> {
}
