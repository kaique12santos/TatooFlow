ALTER TABLE pre_agendamentos ADD COLUMN data_desejada VARCHAR(255);
ALTER TABLE pre_agendamentos ADD COLUMN etapa_bot VARCHAR(50) DEFAULT 'INICIO';
ALTER TABLE pre_agendamentos ADD COLUMN data_atualizacao DATETIME;
