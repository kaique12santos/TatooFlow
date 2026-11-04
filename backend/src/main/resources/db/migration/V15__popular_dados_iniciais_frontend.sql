-- =============================================================================
-- TattooFlow - V15: Carga Inicial de Dados Reais do Frontend
-- Substitui os dados mockados no frontend (clientes, equipe, agendamentos,
-- termos, estoque, financeiro, mídias e postagens) por registros no banco.
-- =============================================================================

-- 1. Usuários da Equipe e Administração
-- Senhas criptografadas com BCrypt (rounds 12) correspondentes aos PINs do sistema:
-- Gabriel Dornelles: PIN 9821
-- Valéria Moretti:   PIN 4119
-- Marcus V.:         PIN 7493
-- Rodrigo Faust:     PIN 0000 (status desligado/inativo)
-- Administrador:     PIN 123456
INSERT INTO usuarios (id, nome, email, senha, perfil, ativo, aparelho_id) VALUES
(1, 'Gabriel Dornelles', 'gabriel@newconcept.art', '$2b$12$l07xvChnNzpS4q0..SEQQO9eNH9KT6nGa/BZ6bFLxUGObmfdZVUou', 'TATUADOR', true, 'tattooflow-gabriel-device-01'),
(2, 'Valéria Moretti', 'valeria@newconcept.art', '$2b$12$lVbl9x2cdTTT7BOByhx6J.vKjJF09GXYJFeGSGQj9n5W/rY2i7OQm', 'TATUADOR', true, 'tattooflow-valeria-device-02'),
(3, 'Marcus V.', 'marcus.v@newconcept.art', '$2b$12$R2MD7JJy8fxnAOntFvNRUeHnyohTAywHqbFMYUMzrNoz2UEReeyAC', 'TATUADOR', true, 'tattooflow-marcus-device-03'),
(4, 'Rodrigo Faust', 'rodrigo.faust@external.art', '$2b$12$AxCxSm6D7SIXXp3doit8AOe3L/k2MB0SnLl1HEIGt0/TiUjpUYP6u', 'TATUADOR', false, 'tattooflow-rodrigo-device-04'),
(5, 'Administrador Geral', 'admin@tattooflow.art', '$2b$12$pZhUWVyDxyBI7kZganGb7eqmlyyOzkJ4mN290svV6vckghSfSJzhy', 'ADMIN', true, 'tattooflow-admin-device-master');

-- 2. Convites de Artistas / Colaboradores
INSERT INTO convites (id, codigo, email_convidado, utilizado, data_expiracao) VALUES
(1, 'tk-lucas-831402', 'lucas.peixoto@newconcept.art', false, '2026-12-31 23:59:59'),
(2, 'tk-clarice-902115', 'clarice.dermato@newconcept.art', false, '2026-12-31 23:59:59'),
(3, 'tk-expirado-102938', 'convidado.antigo@external.art', false, '2026-10-01 12:00:00');

-- 3. Clientes (Dossiês do Frontend)
INSERT INTO clientes (id, nome, telefone, email, instagram, historico_saude) VALUES
(1, 'Camila Albuquerque', '+55 (11) 98844-3321', 'camila.albuquerque@email.com', '@camila_albuquerque', 'Fitzpatrick II. Sem alergias a pigmentos minerais ou orgânicos. Boa retenção de cinzas e cicatrização uniforme.'),
(2, 'Marcos Vinícius Silva', '+55 (11) 97722-1144', 'marcos.silva@email.com', '@marcosv_silva', 'Fitzpatrick III. Sem histórico de queloides ou reações adversas a tintas. Tolerância alta a sessões longas.'),
(3, 'Mariana Rios Barreto', '+55 (21) 99133-8820', 'mariana.rios@email.com', '@marianarios.art', 'Fitzpatrick I. Pele sensível, sem restrições a anestésicos tópicos ou pigmentos veganos. Anamnese aprovada.'),
(4, 'Rodrigo Santoro de Souza', '+55 (11) 96655-4433', 'rodrigo.santoro@email.com', '@rodrigo_santoro_art', 'Fitzpatrick IV. Ótima cicatrização dérmica, sem histórico de diabetes ou problemas de coagulação.');

-- 4. Pré-Agendamentos (Fila de Triagem / Orçamentos do Bot WhatsApp)
INSERT INTO pre_agendamentos (id, cliente_id, ideias_descricao, referencia, local_corpo, data_desejada, status_triagem, etapa_bot, data_criacao, data_atualizacao) VALUES
(1, 1, 'Estudo de São Jerônimo', 'Gravura & Chiaroscuro Renascentista', 'Antebraço Direito', 'Sexta-feira à tarde', 'APROVADO', 'FINALIZADO', '2026-10-10 14:00:00', '2026-10-10 14:30:00'),
(2, 2, 'Fechamento Costas Oriental (Ryū)', 'Irezumi Contemporâneo', 'Dorso Completo', 'Sábados pela manhã', 'PENDENTE', 'FINALIZADO', '2026-11-01 10:15:00', '2026-11-01 10:30:00'),
(3, 3, 'Micro-realismo Botânico', 'Fineline Botânico', 'Costela Esquerda', 'Quinta-feira à tarde', 'APROVADO', 'FINALIZADO', '2026-11-02 16:45:00', '2026-11-02 17:00:00'),
(4, 4, 'Blackwork Ornamental & Geometria Sagrada', 'Blackwork Pesado', 'Manga Completa', 'Terça-feira o dia todo', 'APROVADO', 'FINALIZADO', '2026-09-01 11:00:00', '2026-09-01 11:20:00');

-- 5. Agendamentos de Sessões (Agenda do Estúdio)
INSERT INTO agendamentos (id, cliente_id, tatuador_id, data_hora, duracao_minutos, descricao_sessao, valor_estimado, status) VALUES
(1, 1, 1, '2026-11-05 14:00:00', 240, 'Estudo de São Jerônimo · Sessão 03/03 (Finalização Chiaroscuro)', 1800.00, 'CONFIRMADO'),
(2, 1, 1, '2026-10-16 10:00:00', 240, 'Estudo de São Jerônimo · Sessão 01/03 (Linework & Sombras Base)', 1800.00, 'CONCLUIDO'),
(3, 1, 1, '2026-08-22 14:00:00', 150, 'Peônia Imperial Fine Line · Sessão Única', 1200.00, 'CONCLUIDO'),
(4, 2, 1, '2026-11-06 10:00:00', 300, 'Fechamento Costas Oriental (Ryū) · Sessão 01/05', 2500.00, 'AGENDADO'),
(5, 3, 2, '2026-11-04 14:30:00', 120, 'Micro-realismo Botânico · Costela Esquerda', 950.00, 'EM_ANDAMENTO'),
(6, 4, 3, '2026-09-15 13:00:00', 240, 'Blackwork Ornamental & Geometria Sagrada · Sessão 04/04', 2200.00, 'CONCLUIDO'),
(7, 4, 3, '2026-10-25 15:00:00', 180, 'Retoque Mandala Geometria Sagrada', 600.00, 'CANCELADO'),
(8, 3, 2, '2026-11-01 11:00:00', 180, 'Botânica Floral Fine Line · Antebraço', 1400.00, 'CONCLUIDO');

-- 6. Termos de Consentimento e Anamnese Assinados (RN06 - retenção mínima de 5 anos)
INSERT INTO termos_aceite (id, cliente_id, agendamento_id, url_pdf_assinado, data_assinatura, ip_assinatura, data_expiracao_retencao) VALUES
(1, 1, 2, 'https://storage.tattooflow.art/termos/termo_assinado_camila_sao_jeronimo.pdf', '2026-10-16 09:50:12', '189.120.45.10', '2031-10-16 09:50:12'),
(2, 1, 3, 'https://storage.tattooflow.art/termos/termo_assinado_camila_peonia.pdf', '2026-08-22 13:45:00', '189.120.45.10', '2031-08-22 13:45:00'),
(3, 4, 6, 'https://storage.tattooflow.art/termos/termo_assinado_rodrigo_blackwork.pdf', '2026-09-15 12:40:00', '201.86.110.22', '2031-09-15 12:40:00'),
(4, 3, 8, 'https://storage.tattooflow.art/termos/termo_assinado_mariana_botanica.pdf', '2026-11-01 10:40:00', '177.132.88.15', '2031-11-01 10:40:00');

-- 7. Itens de Estoque e Almoxarifado (Insumos Gerais e Críticos)
INSERT INTO estoque_itens (id, nome, categoria, quantidade, quantidade_minima, unidade_medida) VALUES
(1, 'Cartuchos 03RL Cheyenne', 'AGULHAS', 4, 15, 'unidades'),
(2, 'Tinta Dynamic Black (240ml)', 'TINTAS', 2, 5, 'frascos'),
(3, 'Cartuchos 07MG Magnum', 'AGULHAS', 28, 10, 'unidades'),
(4, 'Luvas Nitrílicas Pretas (M)', 'BIOSSEGURANCA', 180, 40, 'pares'),
(5, 'Stencil Stuff Transfer 250ml', 'ASSEPSIA', 7, 3, 'frascos'),
(6, 'Tiras Indicadoras Biológicas', 'ESTERILIZACAO', 48, 15, 'fitas'),
(7, 'Cartucho Agulha 03RL (Chiaroscuro)', 'AGULHAS', 38, 10, 'un.'),
(8, 'Pigmento Carbon Black Puro (#882)', 'TINTAS', 14, 4, 'un.'),
(9, 'Papel Stencil Hectográfico Especial', 'ASSEPSIA', 92, 20, 'fl.'),
(10, 'Filme Curativo Dérmico Protetor', 'BIOSSEGURANCA', 12, 3, 'rolos'),
(11, 'Vaselina Dermocosmética Notarial', 'ASSEPSIA', 50, 10, 'un.'),
(12, 'Biqueira Descartável Grip 25mm', 'DESCARTAVEIS', 64, 20, 'un.'),
(13, 'Espuma Calmante Epitelial 150ml', 'ASSEPSIA', 22, 8, 'frascos'),
(14, 'Álcool Isopropílico 70% Spray', 'ASSEPSIA', 40, 10, 'un.');

-- 8. Pagamentos (Sinais de Confirmação e Quitações)
INSERT INTO pagamentos (id, agendamento_id, valor, forma_pagamento, status, data_pagamento, tipo, comprovante_nome, comprovante_content_type) VALUES
(1, 1, 900.00, 'PIX', 'CONCLUIDO', '2026-11-03 15:30:00', 'SINAL', 'comprovante_pix_sinal_camila.pdf', 'application/pdf'),
(2, 2, 900.00, 'PIX', 'CONCLUIDO', '2026-10-10 11:00:00', 'SINAL', 'comprovante_sinal_sessao1.pdf', 'application/pdf'),
(3, 2, 900.00, 'PIX', 'CONCLUIDO', '2026-10-16 14:30:00', 'PAGAMENTO', NULL, NULL),
(4, 3, 1200.00, 'CARTAO', 'CONCLUIDO', '2026-08-22 17:00:00', 'PAGAMENTO', NULL, NULL),
(5, 5, 475.00, 'PIX', 'CONCLUIDO', '2026-11-03 18:20:00', 'SINAL', 'comprovante_pix_mariana.pdf', 'application/pdf'),
(6, 6, 2200.00, 'PIX', 'CONCLUIDO', '2026-09-15 17:30:00', 'PAGAMENTO', NULL, NULL),
(7, 8, 1400.00, 'PIX', 'CONCLUIDO', '2026-11-01 14:30:00', 'PAGAMENTO', NULL, NULL);

-- 9. Repasses Financeiros (Divisão 30% Estúdio / 70% Artista com Dedução de Insumos - RN02, RN07)
INSERT INTO repasses (id, tatuador_id, agendamento_id, valor_repasse, valor_liquido, custo_insumos, porcentagem_estudio, data_repasse, status) VALUES
(1, 1, 2, 1225.00, 1225.00, 50.00, 30.00, '2026-10-17 10:00:00', 'PAGO'),
(2, 1, 3, 815.50, 815.50, 35.00, 30.00, '2026-08-23 10:00:00', 'PAGO'),
(3, 3, 6, 1498.00, 1498.00, 60.00, 30.00, '2026-09-16 10:00:00', 'PAGO'),
(4, 2, 8, 952.00, 952.00, 40.00, 30.00, '2026-11-02 09:30:00', 'PAGO'),
(5, 2, 5, 644.00, 644.00, 30.00, 30.00, NULL, 'PENDENTE');

-- 10. Mídias e Portfólio das Obras Realizadas
INSERT INTO midias (id, agendamento_id, url, tipo, data_upload) VALUES
(1, 2, '/assets/sao_jeronimo_tattoo.jpg', 'FOTO_DEPOIS', '2026-10-16 14:35:00'),
(2, 2, '/assets/macro_linework_tattoo.jpg', 'FOTO_DEPOIS', '2026-10-16 14:40:00'),
(3, 3, '/assets/tattoo_healing_check.jpg', 'FOTO_DEPOIS', '2026-08-22 17:15:00'),
(4, 1, '/assets/sao_jeronimo_tattoo.jpg', 'REFERENCIA', '2026-11-03 10:00:00');

-- 11. Envios de Notificações Automáticas (Pré e Pós-Sessão via WhatsApp)
INSERT INTO notificacao_envios (id, agendamento_id, tipo, enviado_em) VALUES
(1, 2, 'PRE', '2026-10-15 10:00:00'),
(2, 2, 'POS', '2026-10-16 18:00:00'),
(3, 3, 'PRE', '2026-08-21 14:00:00'),
(4, 3, 'POS', '2026-08-22 20:00:00'),
(5, 1, 'PRE', '2026-11-04 08:00:00'),
(6, 8, 'PRE', '2026-10-31 11:00:00'),
(7, 8, 'POS', '2026-11-01 17:00:00');

-- 12. Postagens Agendadas e Publicadas no Instagram Graph API [F4-BACK-03, UC03.2]
INSERT INTO postagens_agendadas (id, agendamento_id, midia_id, image_url, legenda, data_hora_agendada, data_hora_publicacao, status, protocolo, instagram_container_id, instagram_media_id, canais, conta_instagram, tentativas, mensagem_erro, created_at, updated_at) VALUES
(1, 2, 1, '/assets/sao_jeronimo_tattoo.jpg', 'Estudo de São Jerônimo · Chiaroscuro Renascentista

A profundidade do claro-escuro renascentista materializada na pele. [Descrição da Arte: ''Estudo anatômico e iluminação dramática de São Jerônimo em sua biblioteca, trabalhado com pigmento Carbon Black puro e agulhas 03RL para texturas milimétricas'']. Executado na região do [Local do Corpo: Antebraço Externo Esquerdo].

#NewConceptTattoo #ChiaroscuroTattoo #RenaissanceArt #BlackAndGreyTattoo #FineLineAtelier #SaoJeronimoTattoo #DarkArtGallery #AtelierMaster', '2026-11-07 20:45:00', NULL, 'AGENDADO', 'NC-2026-1107-78', NULL, NULL, 'FEED,REELS,STORIES,DOSSIE', '@Gabriel_dornelles', 0, NULL, '2026-11-04 08:00:00', '2026-11-04 08:00:00'),
(2, 3, 3, '/assets/tattoo_healing_check.jpg', 'Peônia Imperial Fine Line

Linha única contínua com técnica de micro-pontilhismo nos pistilos e pétalas fluidas. Processo de cicatrização D+30 concluído com perfeição.

#FineLine #Botanico #TattooCurada #NewConceptTattoo', '2026-08-25 19:30:00', '2026-08-25 19:30:05', 'PUBLICADO', 'NC-2026-0825-12', '17928374910293847', '18029384756102938', 'FEED,STORIES', '@newconcept.tattoo', 1, NULL, '2026-08-23 10:00:00', '2026-08-25 19:30:05');

-- 13. Logs de Auditoria e Eventos de Publicação
INSERT INTO postagens_log (id, postagem_agendada_id, tipo_evento, mensagem, detalhes_json, sucesso, data_hora) VALUES
(1, 1, 'AGENDAMENTO_CRIADO', 'Postagem agendada com sucesso para 07/11/2026 20:45 via painel de curadoria', '{"conta": "@Gabriel_dornelles", "preset": "melhor_horario", "canais": ["FEED", "REELS", "STORIES", "DOSSIE"]}', true, '2026-11-04 08:00:00'),
(2, 2, 'PUBLICACAO_SUCESSO', 'Publicação realizada com sucesso no Instagram Graph API', '{"media_id": "18029384756102938", "container_id": "17928374910293847"}', true, '2026-08-25 19:30:05');

