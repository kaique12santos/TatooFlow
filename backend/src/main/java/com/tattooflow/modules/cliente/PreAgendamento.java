package com.tattooflow.modules.cliente;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pre_agendamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreAgendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    private String ideiasDescricao;
    private String localCorpo;
    private String statusTriagem; // PENDENTE, APROVADO, REJEITADO
    private LocalDateTime dataCriacao;
}
