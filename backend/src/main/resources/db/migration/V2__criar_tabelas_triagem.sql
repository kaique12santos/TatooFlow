CREATE TABLE clientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    telefone VARCHAR(50),
    email VARCHAR(255),
    instagram VARCHAR(100),
    historico_saude TEXT
);

CREATE TABLE pre_agendamentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT,
    ideias_descricao TEXT,
    local_corpo VARCHAR(100),
    status_triagem VARCHAR(50) DEFAULT 'PENDENTE',
    data_criacao DATETIME,
    FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);
