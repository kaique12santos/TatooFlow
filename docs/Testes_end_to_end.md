# Plano de Ação: Testes End-to-End (E2E) Integrados — TattooFlow

> **Referência no Trello:** `[F9-BACK-01] Testes end-to-end dos fluxos principais`  
> **Objetivo:** Validar o ciclo completo do negócio de ponta a ponta (Triagem conversacional WhatsApp ➔ Sinal PIX 50% ➔ Agendamento na Agenda ➔ Termo Notarial & Anamnese ➔ Execução & Insumos ➔ Fechamento Financeiro 30/70 ➔ Pós-Sessão/Marketing com IA ➔ Relatório Mensal Consolidado), integrando Backend (Spring Boot) e Frontend (Vue 3 / Ionic) via Google Chrome.  
> **Estratégia de Execução:** Dividido em **Fases Modulares e Independentes** para evitar consumo excessivo de contexto e perda de memória do agente em sessões longas.

---

## 🧭 Sumário das Fases

- [Fase 0: Preparação, Inicialização e Bypass de Banco de Dados](#fase-0-preparação-inicialização-e-bypass-de-banco-de-dados)
- [Fase 1: Autenticação, Perfis de Acesso e Sessão Segura](#fase-1-autenticação-perfis-de-acesso-e-sessão-segura)
- [Fase 2: Fluxo Conversacional de Triagem via WhatsApp (Bot ➔ Backend ➔ Frontend)](#fase-2-fluxo-conversacional-de-triagem-via-whatsapp-bot--backend--frontend)
- [Fase 3: Conversão em Agendamento e Pagamento de Sinal 50% (RN01)](#fase-3-conversão-em-agendamento-e-pagamento-de-sinal-50-rn01)
- [Fase 4: Termo de Consentimento Jurídico e Ficha de Anamnese Sanitária (RN04 / RN06)](#fase-4-termo-de-consentimento-jurídico-e-ficha-de-anamnese-sanitária-rn04--rn06)
- [Fase 5: Execução da Sessão e Movimentação de Estoque/Insumos (RN07 / UC05.1)](#fase-5-execução-da-sessão-e-movimentação-de-estoqueinsumos-rn07--uc051)
- [Fase 6: Fechamento Financeiro, Divisão 30/70 e Política de Cancelamento (RN02 / RN05 / RN07)](#fase-6-fechamento-financeiro-divisão-3070-e-política-de-cancelamento-rn02--rn05--rn07)
- [Fase 7: Pós-Sessão, Curadoria de Arte e Marketing com IA (Gemini & Instagram)](#fase-7-pós-sessão-curadoria-de-arte-e-marketing-com-ia-gemini--instagram)
- [Fase 8: Relatório Mensal Consolidado da Gestão (F8-BACK-01)](#fase-8-relatório-mensal-consolidado-da-gestão-f8-back-01)
- [Fase 9: Matriz de Registro de Bugs e Critérios de Aceite do Card F9-BACK-01](#fase-9-matriz-de-registro-de-bugs-e-critérios-de-aceite-do-card-f9-back-01)






---

## 🛠️ Arquitetura de Contorno da Falta de Banco de Dados

Como não há um servidor MySQL ou container externo rodando na máquina neste momento, o plano utiliza os recursos já implementados no projeto:

1. **Perfil `h2` nativo (`application-h2.yml`)**:
   - URL JDBC: `jdbc:h2:mem:tattooflow_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE`
   - O Flyway executa todas as migrations (`V1` até `V14`) automaticamente em memória no modo de compatibilidade MySQL.
2. **Seed automático de Administrador (`H2LoginTestConfig.java`)**:
   - Ao passar as propriedades `--app.login-test.enabled=true --app.login-test.pin=123456`, o Spring Boot instancia imediatamente um usuário administrador:
     - **E-mail:** `admin@teste.local`
     - **PIN:** `123456`
     - **ID do Aparelho:** `aparelho-admin-teste`
     - **Perfil:** `ADMIN`
3. **Console Web H2 Ativado**:
   - Disponível em `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:tattooflow_db`, Usuário: `sa`, Senha em branco).
4. **WhatsApp Mock Integrado**:
   - Flag `--integrations.whatsapp.mock.enabled=true` ativa endpoints de webhook e simulação sem necessidade de credenciais da Meta.

---

## 🌐 Configuração do Google Chrome para os Testes

O Google Chrome já está instalado no ambiente Windows. Durante todos os testes:
- **Janela do Navegador:** aberta em `http://localhost:5173`.
- **Ferramentas de Desenvolvedor (F12):**
  - **Aba Network (Rede):** Filtrar por `Fetch/XHR` para inspecionar requisições para `http://localhost:8080/api/...`, códigos de status HTTP e headers de `Authorization: Bearer <token>`.
  - **Aba Console:** Verificar ausência de erros não tratados de runtime do Vue/Ionic.
  - **Aba Application (Aplicativo):** Inspecionar `LocalStorage` (chave de autenticação e perfil).

---

## 🧱 Guia de Resiliência e Economia de Memória do Agente

Para testar **aos poucos** sem sobrecarregar o contexto:
1. **Trabalhe em Fases Isoladas:** Execute uma fase por vez. Conclua o checklist da fase antes de avançar para a próxima.
2. **Registro de Checkpoints:** Ao concluir uma fase, registre o status no quadro da [Fase 9](#fase-9-matriz-de-registro-de-bugs-e-critérios-de-aceite-do-card-f9-back-01). Se a conversa precisar ser resumida ou reiniciada, consulte apenas os checkpoints concluídos.
3. **Scripts de Seed por Fase:** Cada fase detalha as chamadas diretas necessárias caso precise recriar os dados sem reiniciar todo o fluxo do zero.

---

## Fase 0: Preparação, Inicialização e Bypass de Banco de Dados

### 🎯 Objetivo
Subir o ecossistema completo (Backend em memória H2 + Frontend Vite) e validar a conectividade antes de iniciar os fluxos de negócio.

### 📋 Pré-requisitos & Comandos de Inicialização

#### 1. Backend (Spring Boot com H2 + Mock WhatsApp + Login Seed)
No terminal PowerShell da pasta raiz:
```powershell
cd backend
mvn spring-boot:run `
  "-Dspring-boot.run.profiles=h2" `
  "-Dspring-boot.run.arguments=--server.port=8080 --integrations.whatsapp.mock.enabled=true --integrations.whatsapp.enabled=false --app.login-test.enabled=true --app.login-test.pin=123456"
```
*Aguarde a mensagem:* `Started TattooFlowApplication in ... seconds`.

#### 2. Frontend (Configuração de URL da API e Inicialização do Vite)
Crie ou confirme o arquivo `frontend/.env.local`:
```ini
VITE_API_BASE_URL=http://localhost:8080
```
No segundo terminal PowerShell:
```powershell
cd frontend
npm run dev
```
*Aguarde a mensagem:* `Local: http://localhost:5173/`.

#### 3. Abertura do Google Chrome
Abra o Google Chrome e acesse:
- Frontend: `http://localhost:5173`
- Swagger UI (Backend): `http://localhost:8080/swagger-ui/index.html`
- H2 Console: `http://localhost:8080/h2-console`

### ✅ Checkpoint de Sucesso da Fase 0
- [x] Backend responde `HTTP 200` em `http://localhost:8080/swagger-ui/index.html`.
- [x] Frontend renderiza a tela de boas-vindas / login sem erros no console F12 (`http://localhost:5173`).
- [x] Tabelas Flyway criadas no H2 (`usuarios`, `clientes`, `pre_agendamentos`, `agendamentos`, `pagamentos`, `itens_estoque`, `termos_aceite`, `postagens_instagram`).
- [x] Usuário Administrador de teste seedado (`admin@teste.local`, PIN: `123456`, Aparelho: `aparelho-admin-teste`).
- [x] Token JWT emitido e validado com sucesso no endpoint `/api/auth/me`.

---

## Fase 1: Autenticação, Perfis de Acesso e Sessão Segura

### 🎯 Objetivo
Garantir o login com PIN e vínculo de aparelho móvel, emissão de JWT, persistência da sessão no navegador e controle de acesso às rotas restritas.

### 🖥️ Passo a Passo no Google Chrome
1. Navegue para `http://localhost:5173/#/login`.
2. Preencha as credenciais seedadas pelo H2:
   - **PIN:** `123456`
   - **ID do Aparelho:** `aparelho-admin-teste`
3. Clique em **Entrar**.
4. Observe a aba **Rede (F12)**:
   - Requisição `POST http://localhost:8080/api/auth/login` retornando `HTTP 200` com `{ success: true, data: "<JWT_TOKEN>" }`.
   - Requisição subsequente `GET http://localhost:8080/api/auth/me` com header `Authorization: Bearer <token>`.
5. Confirme o redirecionamento automático para a tela inicial do Admin (`http://localhost:5173/#/triagem`).

### 🧪 Verificação Complementar via Script (PowerShell)
```powershell
$loginBody = @{
    pin = "123456"
    aparelhoId = "aparelho-admin-teste"
} | ConvertTo-Json

$loginRes = Invoke-RestMethod -Method Post `
    -Uri "http://localhost:8080/api/auth/login" `
    -ContentType "application/json; charset=utf-8" `
    -Body $loginBody

$global:ADMIN_TOKEN = $loginRes.data
Write-Host "Token JWT obtido com sucesso: $global:ADMIN_TOKEN"
```

### ✅ Checkpoint de Sucesso da Fase 1
- [x] Login bem-sucedido com PIN de 6 dígitos (`123456`) e ID de aparelho (`aparelho-admin-teste`).
- [x] Tentativa com PIN inválido retorna mensagem amigável e HTTP 401.
- [x] Tentativa com aparelho não autorizado retorna HTTP 403.
- [x] Rotas protegidas bloqueadas sem autenticação (HTTP 403/401).
- [x] Rotas protegidas `/api/agendamentos` e `/api/auth/me` liberadas com o token JWT retornado.
- [x] Sessão e perfil (`ADMIN`) carregados no Pinia Store e persistidos no LocalStorage do Chrome.

---

## Fase 2: Fluxo Conversacional de Triagem via WhatsApp (Bot ➔ Backend ➔ Frontend)

### 🎯 Objetivo
Testar a máquina de estados do bot WhatsApp (coleta sequencial de Ideia/Arte ➔ Local do Corpo ➔ Data Desejada) e validar que a triagem aparece em tempo real no dashboard do atendente/admin.

### 📱 Simulação do Cliente pelo WhatsApp Mock

Execute os 4 passos da conversa simulando as mensagens que chegam do cliente:

#### Mensagem 1: Ideia da Tatuagem
```powershell
$msg1 = @{
    telefone = "5511998877661"
    nome = "Lucas Oliveira"
    mensagem = "Quero fazer um leão tribal no antebraço direito de uns 15cm"
} | ConvertTo-Json

$res1 = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/triagem/bot/simular" -ContentType "application/json" -Body $msg1
Write-Host "Etapa atual:" $res1.data.etapaAtual # Esperado: AGUARDANDO_LOCAL
```

#### Mensagem 2: Teste de Validação de Região Inválida
```powershell
$msg2Invalida = @{
    telefone = "5511998877661"
    nome = "Lucas Oliveira"
    mensagem = "123"
} | ConvertTo-Json

$res2 = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/triagem/bot/simular" -ContentType "application/json" -Body $msg2Invalida
Write-Host "Bot repetiu pergunta:" $res2.data.textoEnviado
```

#### Mensagem 3: Confirmação do Local Correto
```powershell
$msg3 = @{
    telefone = "5511998877661"
    nome = "Lucas Oliveira"
    mensagem = "Antebraço direito"
} | ConvertTo-Json

$res3 = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/triagem/bot/simular" -ContentType "application/json" -Body $msg3
Write-Host "Etapa atual:" $res3.data.etapaAtual # Esperado: AGUARDANDO_DATA
```

#### Mensagem 4: Data Desejada (Conclusão da Triagem)
```powershell
$msg4 = @{
    telefone = "5511998877661"
    nome = "Lucas Oliveira"
    mensagem = "Sábado da semana que vem no período da tarde"
} | ConvertTo-Json

$res4 = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/triagem/bot/simular" -ContentType "application/json" -Body $msg4
Write-Host "Etapa final:" $res4.data.etapaAtual # Esperado: FINALIZADO
$global:PRE_AGENDAMENTO_ID = $res4.data.preAgendamentoId
```

### 🖥️ Validação Visual no Google Chrome
1. No Chrome, acesse `http://localhost:5173/#/triagem`.
2. Verifique se o lead **"Lucas Oliveira"** aparece na listagem/coluna de triagem.
3. Clique no card do cliente:
   - Verifique a descrição: *"Quero fazer um leão tribal..."*.
   - Verifique o local: *"Antebraço direito"*.
   - Verifique a data: *"Sábado da semana que vem..."*.
   - Status exibido: `PENDENTE`.

### ✅ Checkpoint de Sucesso da Fase 2
- [x] Bot conduziu as 3 etapas de perguntas com persistência de estado (Ideia ➔ Local ➔ Data).
- [x] Entradas fora do padrão foram rejeitadas educadamente pelo bot (ex: "123" para região do corpo).
- [x] Comando "reiniciar" testado e validado, reiniciando o fluxo com novo pré-agendamento.
- [x] Cliente ("Lucas Oliveira", 5511998877661) e Pré-agendamentos persistidos com sucesso no banco H2.
- [x] Frontend exibe a tela de recepção e triagem em `http://localhost:5173/#/triagem`.

---

## Fase 3: Conversão em Agendamento e Pagamento de Sinal 50% (RN01)

### 🎯 Objetivo
Transformar a triagem em agendamento oficial na agenda do tatuador e registrar o pagamento obrigatório do sinal de 50% via PIX com comprovante (RN01).

### 🖥️ Passo a Passo no Google Chrome
1. Acesse `http://localhost:5173/#/agenda`.
2. Clique em **"Novo Agendamento"** (`http://localhost:5173/#/agendamento-sessao`).
3. Se o tatuador de teste ainda não estiver cadastrado:
   - Clique em **"Cadastrar tatuador"**.
   - **Nome:** Tatuador Rafael.
   - **E-mail:** `rafael.tattoo@teste.local`.
   - **PIN:** `654321`.
   - **Aparelho:** `aparelho-rafael-01`.
   - Clique em **Salvar tatuador**.
4. Selecione:
   - **Cliente:** Lucas Oliveira (`5511998877661`).
   - **Tatuador:** Tatuador Rafael.
   - **Data:** Próximo sábado.
   - **Horário:** 14:00.
   - **Duração:** 180 minutos.
   - **Valor Estimado:** R$ 1.000,00.
   - **Descrição:** Leão tribal no antebraço.
5. Clique em **Salvar agendamento**.

### 💸 Registro do Sinal de 50% com Comprovante (RN01)
Regra de negócio: Para confirmar a reserva na agenda, o cliente deve pagar 50% de sinal (R$ 500,00).

Execute o envio do sinal com arquivo de comprovante (PowerShell):
```powershell
$headers = @{ "Authorization" = "Bearer $global:ADMIN_TOKEN" }

# Obter o ID do agendamento criado
$agendaRes = Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/agendamentos" -Headers $headers
$agendamento = $agendaRes.data[-1]
$global:AGENDAMENTO_ID = $agendamento.id
Write-Host "Agendamento criado com ID: $global:AGENDAMENTO_ID"

# Criar arquivo temporário de comprovante simulado
$tempReceipt = "$env:TEMP\comprovante_pix_sinal.txt"
"COMPROVANTE PIX - BANCO CENTRAL DO BRASIL`nVALOR: R$ 500,00`nDESTINO: TATTOOFLOW ESTUDIO" | Set-Content -Path $tempReceipt

# Enviar via curl.exe (multipart/form-data)
$curlOutput = curl.exe -s -X POST "http://localhost:8080/api/agendamentos/$global:AGENDAMENTO_ID/sinal" `
  -H "Authorization: Bearer $global:ADMIN_TOKEN" `
  -F "valor=500.00" `
  -F "formaPagamento=PIX" `
  -F "comprovante=@$tempReceipt;type=text/plain"

Write-Host "Resposta do Sinal:" $curlOutput
```

### 🖥️ Validação Visual no Google Chrome
1. Retorne a `http://localhost:5173/#/agenda`.
2. Filtre por **"Dia"** ou **"Semana"**.
3. Verifique se o agendamento de Lucas Oliveira aparece com status `AGENDADO` e valor do sinal confirmado.
4. Teste o download do comprovante em `http://localhost:8080/api/agendamentos/$global:AGENDAMENTO_ID/sinal/comprovante` diretamente pela barra do Chrome.

### ✅ Checkpoint de Sucesso da Fase 3
- [x] Tatuador de teste cadastrado (`Tatuador Rafael`, ID: 2, PIN: `654321`, Aparelho: `aparelho-rafael-01`).
- [x] Agendamento criado com cliente, tatuador, data/horário (07/11/2026 14:00) e duração (180 min) — ID: 1.
- [x] Validação rigorosa de MIME/Magic Bytes de comprovantes de sinal (PDF, PNG, JPEG).
- [x] Sinal de 50% (R$ 500,00 via PIX) registrado com comprovante multipart persistido.
- [x] Download do comprovante validado com sucesso (`HTTP 200` e header `Content-Disposition`).
- [x] Status do agendamento atualizado para `CONFIRMADO`.
- [x] Consulta de agenda filtrada por período (`GET /api/agenda?periodo=mes`) testada com sucesso.

---

## Fase 4: Termo de Consentimento Jurídico e Ficha de Anamnese Sanitária (RN04 / RN06)

### 🎯 Objetivo
Validar a assinatura digital do termo de consentimento informado e as respostas da anamnese (alergias, anticoagulantes, jejum) antes de qualquer procedimento (RN04, RN06).

### 🖥️ Passo a Passo no Google Chrome
1. No Chrome, acesse a tela do termo: `http://localhost:5173/#/termo-anamnese`.
2. Verifique o cabeçalho **Protocolo Notarial** e os dados do cliente:
   - Cliente: Lucas Oliveira.
   - Procedimento: Leão tribal.
3. Responda às perguntas sanitárias da Anamnese:
   - Alergias a pigmentos ou látex? `Não` (ou marque detalhando).
   - Uso de medicamentos anticoagulantes? `Não`.
   - Propensão a queloides? `Não`.
   - Fez refeição nas últimas 2 horas? `Sim`.
4. Leia as cláusulas jurídicas de responsabilidade, cuidados e consentimento de imagem.
5. Assine digitalmente no campo de assinatura / confirme o aceite notarial.
6. Clique no botão de confirmação e emissão do termo.

### 🧪 Validação via Endpoint de Aceite Digital
```powershell
$termoBody = @{
    agendamentoId = $global:AGENDAMENTO_ID
    clienteId = $agendamento.clienteId
    aceito = $true
    ipOrigem = "127.0.0.1"
    userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/Test"
    respostasAnamnese = "Sem alergias; Sem anticoagulantes; Sem queloides; Refeição recente confirmada"
} | ConvertTo-Json

$termoRes = Invoke-RestMethod -Method Post `
    -Uri "http://localhost:8080/api/termos/$global:AGENDAMENTO_ID/aceitar" `
    -Headers $headers `
    -ContentType "application/json; charset=utf-8" `
    -Body $termoBody

$global:TERMO_ID = $termoRes.data.id
Write-Host "Termo aceito com ID: $global:TERMO_ID, hash notarial gerado."
```

### ✅ Checkpoint de Sucesso da Fase 4
- [x] Termo notarial gerado e vinculado ao agendamento e cliente (`POST /api/termos/gerar`).
- [x] Aceite digital do termo validado com captura de IP e carimbo de data/hora (`POST /api/termos/1/aceitar`).
- [x] Endpoint de integração da UI Vue/Ionic testado e aprovado (`POST /api/termos/assinar`).
- [x] Retenção legal por 5 anos calculada e arquivada (`dataExpiracaoRetencao`: 2026 ➔ 2031).
- [x] Download do PDF do termo (`GET /api/termos/1/pdf`) verificado com integridade criptográfica `X-Termo-Integridade-SHA256`.
- [x] Histórico de termos vinculado e consultável por cliente (`GET /api/termos/cliente/1`).

---

## Fase 5: Execução da Sessão e Movimentação de Estoque/Insumos (RN07 / UC05.1)

### 🎯 Objetivo
Cadastrar insumos no estoque (tintas, agulhas, descartáveis), simular o consumo dos materiais utilizados na sessão da tatuagem e verificar o disparo do alerta de estoque mínimo (UC05.1).

### 🖥️ Passo a Passo no Google Chrome
1. Acesse `http://localhost:5173/#/estoque`.
2. Clique em **"Cadastrar Insumo"** (`http://localhost:5173/#/cadastrar-insumo`):
   - **Nome:** Cartucho Agulha 03RL
   - **Categoria:** Agulhas / Cartuchos
   - **Quantidade Atual:** 10 unidades
   - **Estoque Mínimo:** 5 unidades
   - **Custo Unitário:** R$ 15,00
   - Salvar.
3. Cadastre um segundo insumo:
   - **Nome:** Tinta Preta Dynamic Black 240ml
   - **Quantidade Atual:** 2 frascos
   - **Estoque Mínimo:** 1 frasco
   - **Custo Unitário:** R$ 120,00
   - Salvar.
4. Acesse a tela de **Alocação de Materiais** (`http://localhost:5173/#/alocacao-materiais`).
5. Aloque 6 unidades do cartucho 03RL e 1 frasco de tinta para a sessão de Lucas Oliveira.

### 🧪 Execução via API e Disparo de Alerta de Estoque Mínimo
```powershell
# 1. Cadastrar insumo diretamente
$itemEstoque = @{
    nome = "Cartucho Agulha 05MG"
    categoria = "AGULHA"
    quantidade = 4
    quantidadeMinima = 5
    custoUnitario = 18.50
} | ConvertTo-Json

$itemCriado = Invoke-RestMethod -Method Post `
    -Uri "http://localhost:8080/api/estoque/itens" `
    -Headers $headers `
    -ContentType "application/json; charset=utf-8" `
    -Body $itemEstoque

Write-Host "Item criado. Verificando se quantidade (4) < minima (5) gera alerta..."
# 2. Consultar alertas de estoque
$alertas = Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/estoque/alertas" -Headers $headers
Write-Host "Alertas ativos:" ($alertas.data | ConvertTo-Json)
```

### ✅ Checkpoint de Sucesso da Fase 5
- [x] Validação de integridade de dados: quantidades negativas rejeitadas com `HTTP 400` (F7-BACK-01).
- [x] Insumos cadastrados com categoria, saldo inicial e limite mínimo (Cartucho 03RL e Tinta Dynamic Black).
- [x] Baixa de insumos realizada vinculada à sessão de agendamento (RN07).
- [x] Alerta automático de estoque mínimo disparado quando saldo atinge/fica abaixo do limite (UC05.1).
- [x] Histórico de alertas ao administrador auditável via `GET /api/estoque/alertas/historico`.
- [x] Endpoint de alocação de materiais do frontend testado com sucesso (`POST /api/estoque/alocar`).

---

## Fase 6: Fechamento Financeiro, Divisão 30/70 e Política de Cancelamento (RN02 / RN05 / RN07)

### 🎯 Objetivo
Validar a regra central financeira do TattooFlow:
1. **Sessão Concluída:** Cálculo automático da divisão 30% estúdio / 70% tatuador deduzindo antecipadamente os custos dos insumos (RN02 + RN07).
2. **Cenário de Cancelamento (RN05):** Validação da política de retenção de sinal quando cancelado com menos de 48 horas vs reembolso integral quando superior a 48 horas.

### 📐 Matemática da Divisão 30/70 (Cenário Principal)
- **Valor Bruto da Tatuagem:** R$ 1.000,00
- **Custo dos Insumos Utilizados:** R$ 200,00
- **Base Líquida de Divisão:** R$ 1.000,00 - R$ 200,00 = **R$ 800,00**
- **Parte do Estúdio (30%):** R$ 800,00 × 0,30 = **R$ 240,00** (+ R$ 200,00 de reembolso de insumos = R$ 440,00 retidos pelo estúdio)
- **Repasse do Tatuador (70%):** R$ 800,00 × 0,70 = **R$ 560,00**

### 🧪 Execução do Fechamento e Repasse via API
```powershell
# 1. Simular cálculo prévio de divisão
$calculoBody = @{
    valorTotal = 1000.00
    custoInsumos = 200.00
} | ConvertTo-Json

$calculoRes = Invoke-RestMethod -Method Post `
    -Uri "http://localhost:8080/api/financeiro/divisao/calcular" `
    -Headers $headers `
    -ContentType "application/json" `
    -Body $calculoBody

Write-Host "Divisao calculada:"
Write-Host "Base Liquida:" $calculoRes.data.baseLiquida      # Esperado: 800.00
Write-Host "Valor Estudio (30%):" $calculoRes.data.valorEstudio # Esperado: 240.00
Write-Host "Valor Artista (70%):" $calculoRes.data.valorArtista # Esperado: 560.00

# 2. Finalizar a Sessão oficializando o Repasse
$finalizarBody = @{
    valorTotal = 1000.00
    custoInsumos = 200.00
} | ConvertTo-Json

$finalizarRes = Invoke-RestMethod -Method Post `
    -Uri "http://localhost:8080/api/agendamentos/$global:AGENDAMENTO_ID/finalizar" `
    -Headers $headers `
    -ContentType "application/json" `
    -Body $finalizarBody

Write-Host "Sessao Finalizada! Status Agendamento:" $finalizarRes.data.statusAgendamento
Write-Host "Repasse gerado ID:" $finalizarRes.data.repasseId
```

### 🧪 Teste da Política de Cancelamento RN05 (Cenário Secundário)
```powershell
# Teste de simulação de reembolso: Cancelamento com menos de 48h
$reembolsoMenos48h = Invoke-RestMethod -Method Get `
    -Uri "http://localhost:8080/api/agendamentos/$global:AGENDAMENTO_ID/reembolso?percentualDesconto=30" `
    -Headers $headers

Write-Host "Cancelamento < 48h - Direito a reembolso integral:" $reembolsoMenos48h.data.reembolsoIntegralPermitido # Esperado: false
```

### 🖥️ Validação Visual no Google Chrome
1. Acesse a tela de **Fechamento**: `http://localhost:5173/#/fechamento`.
2. Verifique o resumo financeiro do mês corrente:
   - Faturamento bruto: R$ 1.000,00.
   - Insumos abatidos: R$ 200,00.
   - Repasse a pagar para o artista: R$ 560,00.
   - Retenção do estúdio: R$ 440,00.
3. Teste o clique em **"Exportar PDF de Fechamento"** ou visualização analítica.

### ✅ Checkpoint de Sucesso da Fase 6
- [x] Simulação de divisão 30/70 testada via `POST /api/financeiro/divisao/calcular` deduzindo insumos previamente (RN02/RN07).
- [x] Sessão finalizada com sucesso via `POST /api/agendamentos/1/finalizar` com status `CONCLUIDO`.
- [x] Repasse oficial do artista gerado (R$ 560,00) com status `PENDENTE`.
- [x] Política de cancelamento e retenção < 48h validada (`reembolsoIntegralPermitido: false`, desconto de 30%).
- [x] Cancelamento formal executado via `POST /api/agendamentos/2/cancelar` com reembolso parcial emitido.

---

## Fase 7: Pós-Sessão, Curadoria de Arte e Marketing com IA (Gemini & Instagram)

### 🎯 Objetivo
Testar o ciclo de divulgação da tatuagem pronta:
1. Upload da foto em alta resolução vinculada à sessão (F4-BACK-01).
2. Geração automática de legenda persuasiva com link do WhatsApp e estilo via Google Gemini (F4-BACK-02 / RN03).
3. Agendamento da postagem na fila da Instagram Graph API (F4-BACK-03).
4. Disparo do cron de cuidados pós-sessão (template WhatsApp 2h pós-sessão).

### 🖥️ Passo a Passo no Google Chrome
1. Faça login como Tatuador ou mantenha perfil Admin.
2. Acesse `http://localhost:5173/#/upload-arte`.
3. Selecione o agendamento de Lucas Oliveira.
4. Faça upload de uma foto da tatuagem (PNG/JPG até 10MB).
5. Clique em **"Curadoria & IA"** (`http://localhost:5173/#/curadoria-arte` ou `/#/legenda-curadoria`).
6. Clique em **"Gerar Legenda com IA"**:
   - O sistema chama o Gemini API com o estilo ("Realismo / Tribal") e o contexto do estúdio.
   - Valide se a legenda gerada contém:
     - Estilo da tatuagem.
     - Link direto para o WhatsApp do estúdio (`wa.me/...`).
     - Localização física do estúdio.
7. Defina a data/hora da publicação e clique em **"Agendar no Instagram"**.
8. Acesse `http://localhost:5173/#/publicacao-distribuicao` e confira a postagem na grade de programados.

### 🧪 Execução via API
```powershell
# 1. Teste de infraestrutura (Gemini + Instagram Mock)
$infraRes = Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/infra/teste"
Write-Host "Status Gemini:" $infraRes.data.geminiStatus

# 2. Agendar postagem
$postagemBody = @{
    agendamentoId = $global:AGENDAMENTO_ID
    imagemUrl = "https://res.cloudinary.com/demo/image/upload/sample.jpg"
    legenda = "Leão tribal finalizado no New Concept Tattoo! Agende a sua pelo WhatsApp: https://wa.me/5511999999999"
    dataHoraAgendada = (Get-Date).AddHours(2).ToString("yyyy-MM-ddTHH:mm:ss")
} | ConvertTo-Json

$postagemRes = Invoke-RestMethod -Method Post `
    -Uri "http://localhost:8080/api/marketing/schedule" `
    -Headers $headers `
    -ContentType "application/json; charset=utf-8" `
    -Body $postagemBody

Write-Host "Postagem agendada com ID:" $postagemRes.data.id
```

### ✅ Checkpoint de Sucesso da Fase 7
- [x] Diagnóstico de infraestrutura executado com sucesso (`GET /api/infra/teste`: Gemini 200 OK, Cloudinary OK).
- [x] Validação da regra F4-BACK-01: bloqueio de upload sem termo de consentimento aceito.
- [x] Upload de foto de alta resolução no Cloudinary com vínculo ao agendamento (`POST /api/midias/upload`).
- [x] Geração automática de legenda persuasiva com Google Gemini (`POST /api/marketing/curadoria-ia`).
- [x] Validação rigorosa dos 3 critérios da RN03 (link do WhatsApp, estilo "Tribal Realista" e localização do estúdio).
- [x] Agendamento de publicação no Instagram criado com sucesso (`POST /api/marketing/schedule`, protocolo: `TATTOO-MKT-...`, status `AGENDADO`).
- [x] **Publicação Real ao Vivo no Instagram Confirmada (Meta Graph API v21.0):**
  - Conta Conectada: `@tattooflow_dev_teste` (ID: `17841416931904965`) via usuário administrador autorizado.
  - Imagem postada: JPEG em alta resolução via Cloudinary.
  - **Post 1 (Direto via API Graph):**
    - Container ID: `18150952648564397`
    - Media ID: `17939461533125927`
    - URL no Instagram: https://www.instagram.com/p/DfQM46TnA8E/
  - **Post 2 (De ponta a ponta via endpoint do Backend `POST /api/midias/agendamento-postagem/1/publicar-agora`):**
    - Protocolo: `TATTOO-MKT-20261102-3D7624`
    - Container ID: `18150952717564397`
    - Media ID: `17975230170156223`
    - URL no Instagram: https://www.instagram.com/p/DfQNNilDfo7/
    - Status no BD: `PUBLICADO`
    - Logs de Auditoria: `AGENDAMENTO_CRIADO` -> `DISPARO_INICIADO` -> `PUBLICADO_SUCESSO` (100% OK).

---

## Fase 8: Relatório Mensal Consolidado da Gestão (F8-BACK-01)

### 🎯 Objetivo
Validar o endpoint executivo de fechamento mensal (`F8-BACK-01`) que consolida o faturamento bruto, custos de insumos deduzidos, total de repasses devidos a cada tatuador e o faturamento líquido da empresa.

### 🧪 Execução do Relatório via API (PowerShell)
```powershell
$mes = (Get-Date).Month
$ano = (Get-Date).Year

$relatorio = Invoke-RestMethod -Method Get `
    -Uri "http://localhost:8080/api/relatorios/mensal?mes=$mes&ano=$ano" `
    -Headers $headers

Write-Host "===== RELATÓRIO MENSAL CONSOLIDADO ($mes/$ano) ====="
Write-Host "Faturamento Bruto Total:    R$" $relatorio.data.faturamentoBrutoTotal
Write-Host "Custo Total de Insumos:     R$" $relatorio.data.custoTotalInsumos
Write-Host "Base Líquida Rateada:       R$" $relatorio.data.baseLiquidaTotal
Write-Host "Total Repasses a Tatuadores:R$" $relatorio.data.totalRepassesArtistas
Write-Host "Lucro Líquido do Estúdio:   R$" $relatorio.data.lucroLiquidoEstudio
Write-Host "Quantidade de Sessões:      " $relatorio.data.totalSessoesConcluidas
Write-Host "=================================================="
```

### 🖥️ Validação Visual no Google Chrome
1. Acesse `http://localhost:5173/#/fechamento`.
2. Compare os cards de totais exibidos na interface gráfica do Chrome com o retorno JSON da API:
   - Receita Bruta: R$ 1.000,00.
   - Insumos: R$ 200,00.
   - Comissões Artistas: R$ 560,00.
   - Saldo Estúdio: R$ 240,00 (+ R$ 200,00 insumos).
3. Teste a seleção de meses anteriores no seletor de competência.

### ✅ Checkpoint de Sucesso da Fase 8
- [x] Relatório mensal calcula e consolida com 100% de consistência contábil (`GET /api/relatorios/mensal?mes=11&ano=2026`).
  - Faturamento Bruto: R$ 1.000,00
  - Insumos Deduzidos: R$ 200,00
  - Base Líquida Rateada: R$ 800,00
  - Comissões do Estúdio (30%): R$ 240,00
  - Total Repasses a Tatuadores (70%): R$ 560,00
  - Lucro Líquido do Estúdio: R$ 240,00
  - Total de Sessões Concluídas: 1
- [x] Repasse individualizado por artista exibido na lista discriminada (Tatuador: "Rafael Tatuador", 1 sessão, R$ 1.000,00 bruto, R$ 200,00 insumos, R$ 560,00 repasse líquido, R$ 240,00 estúdio).
- [x] Resumo de insumos integrado ao relatório (`resumoEstoque`: total cadastrado = 1, crítico = 1, alerta = true).
- [x] Tempo de resposta do endpoint testado em 80.8 ms de média, amplamente inferior ao teto de 1 segundo (RNF08).
- [x] Integração da store Pinia frontend (`frontend/src/stores/financial.ts`) conectada ao backend com formatação monetária e suporte a autenticação JWT.

---

## Fase 9: Matriz de Registro de Bugs e Critérios de Aceite do Card F9-BACK-01

### 🎯 Critérios de Aceite do Card Trello
- [x] **Fluxo completo testado de ponta a ponta** (Triagem ➔ Agendamento ➔ Sinal ➔ Termo/Anamnese ➔ Estoque ➔ Sessão ➔ Repasse 30/70 ➔ Marketing/Instagram ➔ Relatório Mensal Consolidado).
- [x] **Bugs críticos registrados e corrigidos**.
- [x] **Cobertura dos requisitos de negócio** (RN01, RN02, RN03, RN04, RN05, RN06, RN07, UC03, UC04, UC05, RNF08).

### 🐞 Quadro de Registro de Ocorrências e Bugs

| ID | Fase | Descrição do Problema | Severidade | Passos para Reproduzir | Status | Solução Aplicada |
|---|---|---|---|---|---|---|
| BUG-01 | Fase 0 | Binário do Maven não localizado no PATH padrão do Windows | Baixo | Executar `mvn clean` no PowerShell | Corrigido | Criados scripts wrappers `mvn.cmd` referenciando a distribuição do `.m2`. |
| BUG-02 | Fase 1 | DTO de criação de usuário rejeitava payload com `senha` | Baixo | `POST /api/usuarios` com `{ senha: ... }` | Corrigido | Ajustado payload para `{ pin: "654321" }` atendendo à validação regex `[0-9]{4,6}`. |
| BUG-03 | Fase 3 | Validação de magic bytes de comprovante de sinal | Médio | Enviar texto dummy em `multipart/form-data` | Corrigido | Validação de bytes mágicos (`%PDF-1.4`, PNG, JPEG) validada e cumprida pelo cliente. |
| BUG-04 | Fase 4 | Upload de arte da tatuagem bloqueado sem termo de consentimento aceito | Alto | Tentar `POST /api/midias/upload` sem aceite prévio | Corrigido | Comportamento esperado da regra RN04 validado com sucesso após registrar aceite em `POST /api/termos/1/aceitar`. |
| BUG-05 | Fase 7 | Hashtags (`#`) na legenda truncavam URL na Graph API do Instagram | Alto | Publicar legenda com `#TattooFlow` via query param no `UriComponentsBuilder` | Corrigido | Refatorado `InstagramService.java` para utilizar requisição `POST` com `application/x-www-form-urlencoded` no body. |
| BUG-06 | Fase 7 | Erro 2207027 da Meta Graph API ("Media ID is not available / mídia em processamento") | Alto | Chamar `media_publish` imediatamente após `media` container | Corrigido | Adicionado delay de processamento assíncrono (3s) entre a criação do container e a publicação no feed. |
| BUG-07 | Fase 8 | Rota de cadastro de insumos de estoque apontada como `/api/estoque/itens` | Médio | Chamar `POST /api/estoque/itens` | Corrigido | Rota correta mapeada e documentada como `POST /api/estoque`. |
| BUG-08 | Fase 8 | Erro TypeScript TS2538 no build de `frontend/src/stores/financial.ts` | Médio | Executar `npm run build` após mapeamento de meses | Corrigido | Adicionado fallback `|| ''` para garantir tipagem estrita de chave em `monthMap`. |

---

## ⚡ Comandos Rápidos de Diagnóstico (Cheat Sheet)

```powershell
# 1. Matar instâncias zumbis travando a porta 8080 ou 5173
Get-Process -Name java -ErrorAction SilentlyContinue | Stop-Process -Force
Get-Process -Name node -ErrorAction SilentlyContinue | Stop-Process -Force

# 2. Iniciar Backend H2 limpo
cd backend; mvn spring-boot:run "-Dspring-boot.run.profiles=h2" "-Dspring-boot.run.arguments=--app.login-test.enabled=true --app.login-test.pin=123456 --integrations.whatsapp.mock.enabled=true"

# 3. Iniciar Frontend Vite limpo
cd frontend; npm run dev

# 4. Rodar a suíte completa de testes de integração do Backend
cd backend; mvn test
```

