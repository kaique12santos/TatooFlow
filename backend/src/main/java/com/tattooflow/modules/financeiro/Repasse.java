package com.tattooflow.modules.financeiro;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "repasses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Repasse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tatuadorId;

    @Column(nullable = false)
    private BigDecimal valorRepasse;

    private BigDecimal porcentagemEstudio;
    private LocalDateTime dataRepasse;
    private String status; // PENDENTE, PAGO
}
