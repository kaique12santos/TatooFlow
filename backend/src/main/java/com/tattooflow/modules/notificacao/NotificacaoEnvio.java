package com.tattooflow.modules.notificacao;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacao_envios", uniqueConstraints =
        @UniqueConstraint(name = "uk_notificacao_agendamento_tipo", columnNames = {"agendamento_id", "tipo"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacaoEnvio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long agendamentoId;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private TipoNotificacao tipo;
    @Column(nullable = false)
    private LocalDateTime enviadoEm;
}
