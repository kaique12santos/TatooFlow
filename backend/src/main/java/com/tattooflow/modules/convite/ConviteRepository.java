package com.tattooflow.modules.convite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ConviteRepository extends JpaRepository<Convite, Long> {
    Optional<Convite> findByCodigo(String codigo);
    
    @Query("""
        SELECT a FROM Convite a
        WHERE a.codigo = :codigo
          AND a.utilizado = false
          AND a.dataExpiracao > :agora
        """)
    Optional<Convite> buscarConviteValido(@Param("codigo") String codigo,
                                          @Param("agora") LocalDateTime agora);
}
