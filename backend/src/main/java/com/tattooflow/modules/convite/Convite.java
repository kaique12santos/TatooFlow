package com.tattooflow.modules.convite;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "convites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Convite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String emailConvidado;

    private Boolean utilzado;
    private LocalDateTime dataExpiracao;
}
