package com.tattooflow.modules.postagem;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidade de auditoria e logs de eventos da publicação no Instagram [F4-BACK-03].
 */
@Entity
@Table(name = "postagens_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostagemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "postagem_agendada_id", nullable = false)
    private Long postagemAgendadaId;

    @Column(name = "tipo_evento", nullable = false, length = 50)
    private String tipoEvento; // AGENDAMENTO_CRIADO, DISPARO_INICIADO, CONTAINER_CRIADO, PUBLICADO_SUCESSO, FALHA_PUBLICACAO

    @Column(name = "mensagem", nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Column(name = "detalhes_json", columnDefinition = "TEXT")
    private String detalhesJson;

    @Column(name = "sucesso", nullable = false)
    @Builder.Default
    private boolean sucesso = true;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @PrePersist
    public void prePersist() {
        if (this.dataHora == null) {
            this.dataHora = LocalDateTime.now();
        }
    }
}

