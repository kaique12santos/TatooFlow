package com.tattooflow.modules.agendamento;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "agendamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clienteId;

    @Column(nullable = false)
    private Long tatuadorId;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    private String descricaoSessao;
    private BigDecimal valorEstimado;
    private String status; // AGENDADO, EM_ANDAMENTO, CONCLUIDO, CANCELADO
}
