-- =============================================================================
-- V14: Tabelas para Agendamento e Publicação no Instagram Graph API [F4-BACK-03, UC03.2]
-- =============================================================================

CREATE TABLE IF NOT EXISTS postagens_agendadas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    agendamento_id BIGINT,
    midia_id BIGINT,
    image_url VARCHAR(1000) NOT NULL,
    legenda TEXT NOT NULL,
    data_hora_agendada DATETIME NOT NULL,
    data_hora_publicacao DATETIME,
    status VARCHAR(50) NOT NULL DEFAULT 'AGENDADO',
    protocolo VARCHAR(100) NOT NULL UNIQUE,
    instagram_container_id VARCHAR(100),
    instagram_media_id VARCHAR(100),
    canais VARCHAR(255),
    conta_instagram VARCHAR(100),
    tentativas INT DEFAULT 0,
    mensagem_erro TEXT,
    created_at DATETIME,
    updated_at DATETIME,
    INDEX idx_postagens_status_data (status, data_hora_agendada),
    INDEX idx_postagens_agendamento (agendamento_id),
    INDEX idx_postagens_protocolo (protocolo)
);

CREATE TABLE IF NOT EXISTS postagens_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    postagem_agendada_id BIGINT NOT NULL,
    tipo_evento VARCHAR(50) NOT NULL,
    mensagem TEXT NOT NULL,
    detalhes_json TEXT,
    sucesso BOOLEAN NOT NULL DEFAULT TRUE,
    data_hora DATETIME NOT NULL,
    INDEX idx_postagens_log_postagem (postagem_agendada_id),
    CONSTRAINT fk_postagens_log_postagem FOREIGN KEY (postagem_agendada_id) REFERENCES postagens_agendadas(id) ON DELETE CASCADE
);

