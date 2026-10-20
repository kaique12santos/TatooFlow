ALTER TABLE pagamentos ADD COLUMN tipo VARCHAR(255) NOT NULL DEFAULT 'PAGAMENTO';
ALTER TABLE pagamentos ADD COLUMN comprovante_nome VARCHAR(255);
ALTER TABLE pagamentos ADD COLUMN comprovante_content_type VARCHAR(255);
ALTER TABLE pagamentos ADD COLUMN comprovante LONGBLOB;

CREATE INDEX idx_pagamentos_agendamento_tipo ON pagamentos (agendamento_id, tipo);
