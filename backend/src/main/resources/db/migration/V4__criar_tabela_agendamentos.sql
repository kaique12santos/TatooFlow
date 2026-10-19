CREATE TABLE agendamentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    tatuador_id BIGINT NOT NULL,
    data_hora DATETIME NOT NULL,
    descricao_sessao VARCHAR(255),
    valor_estimado DECIMAL(38,2),
    status VARCHAR(255)
);

CREATE INDEX idx_agendamentos_tatuador_data ON agendamentos (tatuador_id, data_hora);