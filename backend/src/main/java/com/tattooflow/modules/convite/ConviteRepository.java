package com.tattooflow.modules.convite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConviteRepository extends JpaRepository<Convite, Long> {
    Optional<Convite> findByCodigo(String codigo);
}
