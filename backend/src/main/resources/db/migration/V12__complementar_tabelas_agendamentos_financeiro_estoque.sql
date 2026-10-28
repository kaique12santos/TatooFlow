-- [F5-BANCO-01] Indice dedicado para consulta de agendamentos por data
CREATE INDEX idx_agendamentos_data_hora ON agendamentos (data_hora);

-- [F6-BANCO-01] Complementar tabela de repasses: relacionamento com agendamento, valor liquido e custos de insumos
ALTER TABLE repasses ADD COLUMN agendamento_id BIGINT;
ALTER TABLE repasses ADD COLUMN valor_liquido DECIMAL(38,2);
ALTER TABLE repasses ADD COLUMN custo_insumos DECIMAL(38,2) DEFAULT 0.00;

CREATE INDEX idx_repasses_agendamento ON repasses (agendamento_id);
CREATE INDEX idx_repasses_tatuador_data ON repasses (tatuador_id, data_repasse);

-- [F7-BANCO-01] Indice de alerta de estoque minimo e View de compatibilidade estoque sem duplicar estoque_itens
CREATE INDEX idx_estoque_itens_qtd_min ON estoque_itens (quantidade, quantidade_minima);

CREATE VIEW estoque AS
SELECT 
    id,
    nome AS item,
    categoria,
    quantidade,
    quantidade_minima,
    unidade_medida
FROM estoque_itens;
