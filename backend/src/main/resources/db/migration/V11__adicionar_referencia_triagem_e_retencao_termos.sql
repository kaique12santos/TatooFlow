-- F2-BANCO-01: Adicionar campo de referencia na tabela pre_agendamentos
ALTER TABLE pre_agendamentos ADD COLUMN referencia TEXT;

-- F3-BANCO-01: Adicionar campo de expiracao de retencao (RN06 - minimo 5 anos) na tabela termos_aceite
ALTER TABLE termos_aceite ADD COLUMN data_expiracao_retencao DATETIME;

-- F3-BANCO-01: Relacionamentos de chave estrangeira para termos_aceite com clientes e agendamentos
ALTER TABLE termos_aceite ADD CONSTRAINT fk_termos_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id);
ALTER TABLE termos_aceite ADD CONSTRAINT fk_termos_agendamento FOREIGN KEY (agendamento_id) REFERENCES agendamentos (id);
