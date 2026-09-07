package com.tattooflow.modules.estoque;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estoque_itens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String categoria; // AGULHAS, TINTAS, LUVAS, DESCARTAVEIS
    private Integer quantidade;
    private Integer quantidadeMinima;
    private String unidadeMedida;
}
