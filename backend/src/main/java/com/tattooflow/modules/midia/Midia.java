package com.tattooflow.modules.midia;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "midias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Midia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long agendamentoId;

    @Column(nullable = false)
    private String url;

    private String tipo; // FOTO_ANTES, FOTO_DEPOIS, REFERENCIA
    private LocalDateTime dataUpload;
}
