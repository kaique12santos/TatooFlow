package com.tattooflow.modules.termo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TermoRepository extends JpaRepository<TermoAceite, Long> {

    List<TermoAceite> findByClienteId(Long clienteId);

    List<TermoAceite> findByClienteIdOrderByDataAssinaturaDesc(Long clienteId);

    List<TermoAceite> findByAgendamentoId(Long agendamentoId);

    Optional<TermoAceite> findFirstByAgendamentoIdOrderByIdDesc(Long agendamentoId);

    boolean existsByAgendamentoIdAndDataAssinaturaIsNotNull(Long agendamentoId);

    boolean existsByClienteIdAndDataAssinaturaIsNotNull(Long clienteId);
}
