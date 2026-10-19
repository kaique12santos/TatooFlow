package com.tattooflow.modules.cliente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PreAgendamentoRepository extends JpaRepository<PreAgendamento, Long> {
    Optional<PreAgendamento> findFirstByClienteOrderByDataCriacaoDesc(Cliente cliente);
    Optional<PreAgendamento> findFirstByClienteAndStatusTriagemOrderByDataCriacaoDesc(Cliente cliente, String statusTriagem);
    List<PreAgendamento> findByStatusTriagem(String statusTriagem);
    List<PreAgendamento> findByCliente(Cliente cliente);
}
