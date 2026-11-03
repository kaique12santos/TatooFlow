package com.tattooflow.modules.postagem;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidade que representa uma postagem no Instagram com agendamento [F4-BACK-03, UC03.2].
 */
@Entity
@Table(name = "postagens_agendadas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostagemAgendada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agendamento_id")
    private Long agendamentoId;

    @Column(name = "midia_id")
    private Long midiaId;

    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;

    @Column(name = "legenda", nullable = false, columnDefinition = "TEXT")
    private String legenda;

    @Column(name = "data_hora_agendada", nullable = false)
    private LocalDateTime dataHoraAgendada;

    @Column(name = "data_hora_publicacao")
    private LocalDateTime dataHoraPublicacao;

    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private String status = "AGENDADO"; // AGENDADO, PUBLICANDO, PUBLICADO, ERRO, CANCELADO

    @Column(name = "protocolo", nullable = false, unique = true, length = 100)
    private String protocolo;

    @Column(name = "instagram_container_id", length = 100)
    private String instagramContainerId;

    @Column(name = "instagram_media_id", length = 100)
    private String instagramMediaId;

    @Column(name = "canais", length = 255)
    private String canais;

    @Column(name = "conta_instagram", length = 100)
    private String contaInstagram;

    @Column(name = "tentativas")
    @Builder.Default
    private Integer tentativas = 0;

    @Column(name = "mensagem_erro", columnDefinition = "TEXT")
    private String mensagemErro;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = "AGENDADO";
        }
        if (this.tentativas == null) {
            this.tentativas = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

