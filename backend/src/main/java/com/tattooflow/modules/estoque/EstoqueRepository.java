package com.tattooflow.modules.estoque;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstoqueRepository extends JpaRepository<ItemEstoque, Long> {

    List<ItemEstoque> findByCategoriaIgnoreCase(String categoria);

    @Query("SELECT e FROM ItemEstoque e WHERE e.quantidade <= e.quantidadeMinima ORDER BY e.quantidade ASC")
    List<ItemEstoque> buscarItensEmEstoqueMinimo();

    @Query("""
        SELECT e FROM ItemEstoque e
        WHERE LOWER(e.nome) LIKE LOWER(CONCAT('%', :termo, '%'))
           OR LOWER(e.categoria) LIKE LOWER(CONCAT('%', :termo, '%'))
        ORDER BY e.nome ASC
    """)
    List<ItemEstoque> buscarPorTermo(@Param("termo") String termo);
}
