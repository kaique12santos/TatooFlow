CREATE TABLE notificacao_envios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    agendamento_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    enviado_em DATETIME NOT NULL,
    CONSTRAINT uk_notificacao_agendamento_tipo UNIQUE (agendamento_id, tipo),
    CONSTRAINT fk_notificacao_agendamento FOREIGN KEY (agendamento_id) REFERENCES agendamentos (id)
);

CREATE INDEX idx_agendamentos_status_data ON agendamentos (status, data_hora);
