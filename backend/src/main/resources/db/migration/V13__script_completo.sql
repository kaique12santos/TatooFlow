-- =============================================================================
-- TattooFlow - Script Completo Consolidado do Banco de Dados (Versão Final V13)
-- Contém a estrutura completa unificada para implantação direta no MySQL 8.0+
-- =============================================================================

-- 1. Tabela: usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    perfil VARCHAR(50) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    aparelho_id VARCHAR(255)
);

-- 2. Tabela: convites
CREATE TABLE IF NOT EXISTS convites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(100) NOT NULL UNIQUE,
    email_convidado VARCHAR(255) NOT NULL,
    utilizado BOOLEAN DEFAULT FALSE,
    data_expiracao DATETIME
);

-- 3. Tabela: clientes
CREATE TABLE IF NOT EXISTS clientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    telefone VARCHAR(50),
    email VARCHAR(255),
    instagram VARCHAR(100),
    historico_saude TEXT
);

-- 4. Tabela: pre_agendamentos
CREATE TABLE IF NOT EXISTS pre_agendamentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT,
    ideias_descricao TEXT,
    referencia TEXT,
    local_corpo VARCHAR(100),
    data_desejada VARCHAR(255),
    status_triagem VARCHAR(50) DEFAULT 'PENDENTE',
    etapa_bot VARCHAR(50) DEFAULT 'INICIO',
    data_criacao DATETIME,
    data_atualizacao DATETIME,
    CONSTRAINT fk_pre_agendamentos_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

-- 5. Tabela: agendamentos
CREATE TABLE IF NOT EXISTS agendamentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    tatuador_id BIGINT NOT NULL,
    data_hora DATETIME NOT NULL,
    duracao_minutos INT NOT NULL DEFAULT 60,
    descricao_sessao VARCHAR(255),
    valor_estimado DECIMAL(38,2),
    status VARCHAR(255),
    INDEX idx_agendamentos_tatuador_data (tatuador_id, data_hora),
    INDEX idx_agendamentos_status_data (status, data_hora),
    INDEX idx_agendamentos_data_hora (data_hora)
);

-- 6. Tabela: termos_aceite
CREATE TABLE IF NOT EXISTS termos_aceite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    agendamento_id BIGINT NOT NULL,
    url_pdf_assinado VARCHAR(500) NOT NULL,
    data_assinatura DATETIME,
    ip_assinatura VARCHAR(50),
    data_expiracao_retencao DATETIME,
    CONSTRAINT fk_termos_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_termos_agendamento FOREIGN KEY (agendamento_id) REFERENCES agendamentos(id)
);

-- 7. Tabela: estoque_itens
CREATE TABLE IF NOT EXISTS estoque_itens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    categoria VARCHAR(255),
    quantidade INT,
    quantidade_minima INT,
    unidade_medida VARCHAR(255),
    INDEX idx_estoque_itens_qtd_min (quantidade, quantidade_minima)
);

-- 8. Tabela: pagamentos
CREATE TABLE IF NOT EXISTS pagamentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    agendamento_id BIGINT NOT NULL,
    valor DECIMAL(38,2) NOT NULL,
    forma_pagamento VARCHAR(255),
    status VARCHAR(255),
    data_pagamento DATETIME,
    tipo VARCHAR(255) NOT NULL DEFAULT 'PAGAMENTO',
    comprovante_nome VARCHAR(255),
    comprovante_content_type VARCHAR(255),
    comprovante LONGBLOB,
    INDEX idx_pagamentos_agendamento (agendamento_id),
    INDEX idx_pagamentos_agendamento_tipo (agendamento_id, tipo)
);

-- 9. Tabela: repasses
CREATE TABLE IF NOT EXISTS repasses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tatuador_id BIGINT NOT NULL,
    agendamento_id BIGINT,
    valor_repasse DECIMAL(38,2) NOT NULL,
    valor_liquido DECIMAL(38,2),
    custo_insumos DECIMAL(38,2) DEFAULT 0.00,
    porcentagem_estudio DECIMAL(38,2),
    data_repasse DATETIME,
    status VARCHAR(255),
    INDEX idx_repasses_agendamento (agendamento_id),
    INDEX idx_repasses_tatuador_data (tatuador_id, data_repasse)
);

-- 10. Tabela: midias
CREATE TABLE IF NOT EXISTS midias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    agendamento_id BIGINT NOT NULL,
    url VARCHAR(255) NOT NULL,
    tipo VARCHAR(255),
    data_upload DATETIME,
    INDEX idx_midias_agendamento (agendamento_id)
);

-- 11. Tabela: notificacao_envios
CREATE TABLE IF NOT EXISTS notificacao_envios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    agendamento_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    enviado_em DATETIME NOT NULL,
    CONSTRAINT uk_notificacao_agendamento_tipo UNIQUE (agendamento_id, tipo),
    CONSTRAINT fk_notificacao_agendamento FOREIGN KEY (agendamento_id) REFERENCES agendamentos(id)
);

-- 12. View: estoque
CREATE OR REPLACE VIEW estoque AS
SELECT 
    id,
    nome AS item,
    categoria,
    quantidade,
    quantidade_minima,
    unidade_medida
FROM estoque_itens;
