package com.tattooflow.modules.termo;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "termos_aceite")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TermoAceite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clienteId;

    @Column(nullable = false)
    private Long agendamentoId;

    @Column(nullable = false)
    private String urlPdfAssinado;

    private LocalDateTime dataAssinatura;
    private String ipAssinatura;
}
