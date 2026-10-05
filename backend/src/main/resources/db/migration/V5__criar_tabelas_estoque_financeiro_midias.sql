CREATE TABLE estoque_itens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    categoria VARCHAR(255),
    quantidade INT,
    quantidade_minima INT,
    unidade_medida VARCHAR(255)
);

CREATE TABLE pagamentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    agendamento_id BIGINT NOT NULL,
    valor DECIMAL(38,2) NOT NULL,
    forma_pagamento VARCHAR(255),
    status VARCHAR(255),
    data_pagamento DATETIME
);

CREATE TABLE repasses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tatuador_id BIGINT NOT NULL,
    valor_repasse DECIMAL(38,2) NOT NULL,
    porcentagem_estudio DECIMAL(38,2),
    data_repasse DATETIME,
    status VARCHAR(255)
);

CREATE TABLE midias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    agendamento_id BIGINT NOT NULL,
    url VARCHAR(255) NOT NULL,
    tipo VARCHAR(255),
    data_upload DATETIME
);

CREATE INDEX idx_pagamentos_agendamento ON pagamentos (agendamento_id);
CREATE INDEX idx_midias_agendamento ON midias (agendamento_id);