package com.tattooflow.modules.cliente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByTelefone(String telefone);

    List<Cliente> findAllByOrderByNomeAsc();

    /**
     * Busca clientes por nome, email, instagram, telefone ou por tipo de trabalho/ideia
     * descritos em seus pré-agendamentos (orçamentos) e sessões (agendamentos) [F5-BACK-02, UC04.1].
     */
    @Query("""
        SELECT DISTINCT c FROM Cliente c
        WHERE (:termo IS NULL OR :termo = ''
               OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%'))
               OR LOWER(c.email) LIKE LOWER(CONCAT('%', :termo, '%'))
               OR LOWER(c.instagram) LIKE LOWER(CONCAT('%', :termo, '%'))
               OR LOWER(c.telefone) LIKE LOWER(CONCAT('%', :termo, '%'))
               OR EXISTS (
                   SELECT 1 FROM PreAgendamento p
                   WHERE p.cliente = c
                     AND (LOWER(p.ideiasDescricao) LIKE LOWER(CONCAT('%', :termo, '%'))
                          OR LOWER(p.referencia) LIKE LOWER(CONCAT('%', :termo, '%')))
               )
               OR EXISTS (
                   SELECT 1 FROM Agendamento a
                   WHERE a.clienteId = c.id
                     AND LOWER(a.descricaoSessao) LIKE LOWER(CONCAT('%', :termo, '%'))
               )
        )
        ORDER BY c.nome ASC
        """)
    List<Cliente> buscarPorNomeOuTipoTrabalho(@Param("termo") String termo);
}
