# Backend TattooFlow (Spring Boot 3 + Java 17)

## Estrutura de Pacotes

A aplicação adota o padrão **MVC em Camadas organizado por Módulos de Domínio** (`modules/`):

- `config/`: Configurações transversais (Security, Swagger, WebClient, CORS).
- `common/`: Respostas padrão, tratamento global de exceções, utilitários JWT.
- `modules/`: Módulos de negócio isolados (auth, usuario, convite, cliente, termo, midia, agendamento, financeiro, estoque, relatorio, notificacao).
- `integrations/`: Comunicação isolada com APIs externas (WhatsApp, Gemini AI, Instagram).

## Comandos Úteis

Para configurar o sandbox da Meta e validar o webhook de teste (F2-INFRA-01), consulte
[Configuração do WhatsApp Business API](../docs/whatsapp-sandbox.md).

Para simular mensagens recebidas sem conta ou credenciais Meta (F2-INFRA-02), consulte
[Mock local do WhatsApp](../docs/whatsapp-mock.md).

Para configurar os jobs, prazos e templates de cuidados pré/pós-sessão (F2-BACK-04 / UC08),
consulte [Notificações automáticas de cuidados](../docs/notificacoes-cuidados.md).

```bash
# Executar a aplicação
mvn spring-boot:run

# Executar testes unitários
mvn test

# Compilar projeto
mvn clean compile
```

## Login com PIN e ID do aparelho (F1-BACK-02)

Para executar sem Docker/MySQL e cadastrar os dados manualmente, consulte também
o roteiro de [execução local com H2](#execução-local-com-h2).

`POST /auth/login` valida o PIN e o aparelho autorizado de um usuário ativo com perfil `ADMIN` ou `TATUADOR`. A rota `/api/auth/login` aceita o mesmo contrato. Ambas são públicas e recebem JSON:

```json
{
  "pin": "012345",
  "aparelhoId": "aparelho-tatuador-01"
}
```

O PIN deve ser uma string de 4 a 6 dígitos, preservando zeros iniciais. `aparelhoId` é obrigatório, tem no máximo 255 caracteres e deve corresponder ao aparelho previamente autorizado. Cada usuário tem um aparelho, e o mesmo ID não pode ser vinculado a dois usuários.

Em caso de sucesso, a resposta é HTTP 200 com `success: true` e o JWT no campo `data`. O login antigo por `email` e `senha` deixou de gerar tokens.

| HTTP | Situação | Mensagem |
| --- | --- | --- |
| 400 | Campos ausentes, PIN fora do formato ou JSON inválido | Mensagem de validação |
| 401 | PIN incorreto para o usuário do aparelho | `PIN inválido` |
| 403 | Aparelho sem vínculo cadastrado | `Aparelho não autorizado` |
| 403 | Usuário desativado | `Usuário inativo` |
| 403 | Perfil diferente de ADMIN/TATUADOR | `Usuário não autorizado` |
| 415 | Content-Type diferente de application/json | `Content-Type não suportado para este endpoint` |

As respostas de erro usam `success: false`, `message` e `data: null`.

### Cadastro de PIN e aparelho

A migration `V7__adicionar_aparelho_usuario.sql` adiciona `usuarios.aparelho_id` com restrição de unicidade. Ela não altera os registros existentes. O campo `usuarios.senha` passa a armazenar o hash bcrypt do PIN, com custo 12; o PIN e o hash não são retornados nos endpoints de usuário.

Um administrador autenticado pode cadastrar usuários e autorizar seus aparelhos usando `POST /api/usuarios`:

```json
{
  "nome": "Nome do tatuador",
  "email": "tatuador@exemplo.com",
  "pin": "012345",
  "aparelhoId": "aparelho-tatuador-01",
  "perfil": "TATUADOR"
}
```

O serviço aplica bcrypt antes de salvar. O endpoint aceita `pin` em vez de `senha`; o usuário é criado ativo. O cadastro exige JWT de `ADMIN`, evitando que um login de tatuador autorize outro aparelho ou crie um administrador.

### Primeiro administrador e usuários antigos

O login depende do usuário/aparelho previamente cadastrado (F1-BACK-01). Em uma base vazia, o primeiro administrador precisa ser provisionado diretamente no banco. Usuários antigos precisam de um aparelho vinculado e de um PIN redefinido com bcrypt; credenciais legadas em texto puro são rejeitadas.

Para gerar o hash localmente com as dependências deste backend, depois de compilar, execute no PowerShell:

```powershell
$cryptoJar = Join-Path $env:USERPROFILE '.m2/repository/org/springframework/security/spring-security-crypto/6.2.2/spring-security-crypto-6.2.2.jar'
$loggingJar = Join-Path $env:USERPROFILE '.m2/repository/org/springframework/spring-jcl/6.1.4/spring-jcl-6.1.4.jar'
jshell --class-path "$cryptoJar;$loggingJar"
```

No JShell, substitua o PIN do exemplo pelo PIN escolhido e copie o hash retornado:

```java
new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(12).encode("012345");
```

No banco usado pelo backend, substitua os valores do exemplo, incluindo `<hash-bcrypt-gerado>`:

```sql
-- Primeiro administrador, quando ainda não existe usuário provisionado:
INSERT INTO usuarios (nome, email, senha, aparelho_id, perfil, ativo)
VALUES ('Administrador', 'admin@exemplo.com', '<hash-bcrypt-gerado>', 'aparelho-admin-01', 'ADMIN', TRUE);

-- Alternativa para redefinir a credencial de um usuário existente:
UPDATE usuarios
SET senha = '<hash-bcrypt-gerado>', aparelho_id = 'aparelho-tatuador-01'
WHERE email = 'tatuador@exemplo.com';
```

Depois faça o login com o PIN escolhido e o ID cadastrado. O backend precisa ser reiniciado para carregar o novo código e aplicar a migration; no perfil `h2`, cadastre os dados na instância em execução, pois o banco é em memória.


## Consultar agendamentos salvos

`GET /api/agendamentos` consulta a tabela `agendamentos` do banco configurado no perfil ativo e retorna todos os registros, incluindo os cancelados. A lista fica no campo `data` da resposta. Quando nao ha registros, retorna HTTP 200 com `data: []`.

A rota exige o cabecalho `Authorization: Bearer <token>`. No PowerShell, obtenha o token pela rota de login e consulte os agendamentos:

```powershell
$loginBody = @{ pin = "012345"; aparelhoId = "aparelho-tatuador-01" } | ConvertTo-Json
$login = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/auth/login" -ContentType "application/json" -Body $loginBody
$headers = @{ Authorization = "Bearer $($login.data)" }
$resposta = Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/agendamentos" -Headers $headers
$resposta.data
```

Para consultar somente os registros de um tatuador, use `GET /api/agendamentos/tatuador/{tatuadorId}` com o mesmo cabecalho.

### Usar JWT no Swagger

1. Com o backend em execução, abra `http://localhost:8080/swagger-ui/index.html`.
2. Expanda `POST /auth/login`, clique em **Try it out**, preencha o corpo JSON com `pin` e `aparelhoId` de um usuário já cadastrado e clique em **Execute**.
3. Copie somente o token do campo `data` da resposta, sem as aspas.
4. Clique em **Authorize**, cole o token no campo de `bearerAuth` **sem escrever `Bearer `** e confirme em **Authorize**. Depois clique em **Close**.
5. Expanda `GET /api/agendamentos`, clique em **Try it out** e depois em **Execute**. O Swagger envia automaticamente `Authorization: Bearer <token>` e a lista aparece em `data`.

A mesma autorização funciona em `GET /api/agendamentos/tatuador/{tatuadorId}` e `POST /api/agendamentos`. O login permanece disponível sem token.

Se o token expirar ou o backend reiniciar, faça login novamente e substitua o token em **Authorize**. O JWT atual dura 24 horas e usa uma chave gerada a cada inicialização. Sem um JWT válido, as rotas de agendamentos retornam HTTP 403 na configuração atual.

O JWT identifica o usuário cadastrado pelo e-mail. As requisições autenticadas consultam esse usuário no banco; usuários removidos ou desativados deixam de acessar os endpoints protegidos.

Exemplo dos dados de um agendamento no campo `data`:

```json
[
  {
    "id": 1,
    "clienteId": 1,
    "tatuadorId": 1,
    "dataHora": "2026-10-10T12:00:00",
    "duracaoMinutos": 120,
    "descricaoSessao": "Fechamento de braco",
    "valorEstimado": 800.00,
    "status": "AGENDADO"
  }
]
```

O perfil `h2` usa um banco em memoria: os dados inseridos ficam disponiveis enquanto a aplicacao estiver em execucao e sao perdidos ao reiniciar. Para consultar dados existentes, use a mesma instancia e o mesmo perfil usados na insercao.

## Execução local com H2

1. Na pasta `backend/`, inicie a aplicação com o perfil `h2`:

   ```powershell
   mvn spring-boot:run "-Dspring-boot.run.profiles=h2" "-Dspring-boot.run.arguments=--server.address=127.0.0.1"
   ```

   É necessário ter Java e Maven disponíveis no terminal. Se `mvn` não for reconhecido,
   use o caminho completo do `mvn.cmd` da instalação local ou configure o Maven no `PATH`.

2. Aguarde a aplicação iniciar. O H2 roda dentro do backend e as tabelas são criadas
   pelas migrations do Flyway; não é necessário iniciar um servidor de banco separado.
3. Abra `http://localhost:8080/h2-console` e use os dados de `src/main/resources/application-h2.yml`:

   | Campo | Valor |
   | --- | --- |
   | Driver Class | `org.h2.Driver` |
   | JDBC URL | `jdbc:h2:mem:tattooflow_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE` |
   | User Name | `sa` |
   | Password | Deixe em branco |

4. Siga [Primeiro administrador e usuários antigos](#primeiro-administrador-e-usuários-antigos)
   neste arquivo para gerar o hash bcrypt do PIN e executar o `INSERT INTO usuarios` no console H2.
5. Faça login com o PIN e o `aparelhoId` cadastrados. Use o JWT retornado para acessar os endpoints
   ou autorizar o Swagger em `http://localhost:8080/swagger-ui/index.html`.

O banco é em memória: mantenha o backend aberto e repita a inserção dos dados após cada reinício.
O comando com o mock de WhatsApp habilitado está em [docs/whatsapp-mock.md](../docs/whatsapp-mock.md).

## F2-BACK-03 — Registrar sinal de 50% (RN01)

O sinal é registrado com upload do comprovante em
`POST /api/agendamentos/{id}/sinal`, autenticado com JWT e `multipart/form-data`:

| Campo | Regra |
| --- | --- |
| `valor` | Obrigatório; exatamente 50% de `valorEstimado`, usando ponto decimal |
| `comprovante` | Obrigatório; arquivo PDF, PNG ou JPEG de até 5 MB |
| `formaPagamento` | `PIX` (padrão), `CARTAO` ou `DINHEIRO` |

Exemplo no PowerShell, para um agendamento de R$ 350,00:

```powershell
curl.exe -X POST "http://localhost:8080/api/agendamentos/1/sinal" `
  -H "Authorization: Bearer SEU_TOKEN" `
  -F "valor=175.00" `
  -F "formaPagamento=PIX" `
  -F "comprovante=@comprovante.pdf;type=application/pdf"
```

O retorno HTTP `201` contém o pagamento de tipo `SINAL`, status `CONCLUIDO`, data e
metadados do arquivo. O conteúdo do arquivo fica armazenado no banco e não aparece no JSON.
Para baixar o comprovante, use `GET /api/agendamentos/{id}/sinal/comprovante`, também autenticado.

Somente agendamentos `AGENDADO` com valor estimado positivo podem receber sinal.
O cálculo usa duas casas decimais e arredondamento `HALF_UP`: para R$ 100,01, o sinal é R$ 50,01.
A assinatura inicial do arquivo e seu tipo declarado são verificados. Esse registro não consulta
o banco pagador para autenticar a transferência.

O pagamento, o comprovante e a mudança para `CONFIRMADO` são gravados na mesma transação;
uma falha reverte a operação inteira. O bloqueio do agendamento impede sinais duplicados
em requisições simultâneas e é compartilhado com a edição do agendamento.

A criação/edição não permite `CONFIRMADO`, `EM_ANDAMENTO` ou `CONCLUIDO` sem sinal registrado.
Após o sinal, o valor estimado deve continuar compatível com os 50% pagos. A edição de duração,
horário e as validações de conflito existentes continuam disponíveis. O endpoint genérico de
pagamentos não aceita tipo `SINAL` nem sobrescrita por ID; pagamentos comuns não confirmam a sessão.

Erros de regra ou campos inválidos retornam `400`; recursos ausentes, `404`; requisições que
não são multipart, `415`; uploads acima dos limites do servidor, `413`. O limite total da requisição
é 6 MB. A migration `V9__adicionar_sinal_comprovante_pagamentos.sql` acrescenta os campos ao
banco existente e classifica os pagamentos anteriores como `PAGAMENTO`.

Os testes da RN01 incluem upload/download autenticado, persistência e migrations no perfil H2,
proteção contra confirmação direta, rollback e concorrência.


## F2-BANCO-01 e F3-BANCO-01 — Triagem, Termos de Aceite e Retenção Legal (RN06)

A migration `V11__adicionar_referencia_triagem_e_retencao_termos.sql` atualiza o esquema do banco de dados para contemplar todos os critérios de aceite das tarefas:

1. **[F2-BANCO-01] Pré-agendamento / Triagem**:
   - Tabela `pre_agendamentos` com suporte aos campos de referência (`referencia`), local do corpo (`local_corpo`), data desejada (`data_desejada`) e status (`status_triagem`).
   - Relacionamento de chave estrangeira com a tabela `clientes`.

2. **[F3-BANCO-01] Termos / Histórico de Aceite**:
   - Tabela `termos_aceite` vinculada ao cliente (`cliente_id`) e ao agendamento (`agendamento_id`) por chaves estrangeiras com índices.
   - Registro de data/hora (`data_assinatura`), IP (`ip_assinatura`) e URL do PDF gerado/assinado (`url_pdf_assinado`).
   - Campo de expiração de retenção legal (`data_expiracao_retencao`): atende à **RN06**, garantindo prazo mínimo de retenção de 5 anos calculados a partir da data de assinatura.
