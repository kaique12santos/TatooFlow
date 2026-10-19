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
    private String dataDesejada;
    private String statusTriagem; // EM_TRIAGEM, PENDENTE, APROVADO, REJEITADO
    private String etapaBot; // INICIO, AGUARDANDO_ARTE, AGUARDANDO_LOCAL, AGUARDANDO_DATA, FINALIZADO
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
}
