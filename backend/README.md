# Backend TattooFlow (Spring Boot 3 + Java 17)

## Estrutura de Pacotes

A aplicação adota o padrão **MVC em Camadas organizado por Módulos de Domínio** (`modules/`):

- `config/`: Configurações transversais (Security, Swagger, WebClient, CORS).
- `common/`: Respostas padrão, tratamento global de exceções, utilitários JWT.
- `modules/`: Módulos de negócio isolados (auth, usuario, convite, cliente, termo, midia, agendamento, financeiro, estoque, relatorio, notificacao).
- `integrations/`: Comunicação isolada com APIs externas (WhatsApp, Gemini AI, Instagram).

## Comandos Úteis

```bash
# Executar a aplicação
mvn spring-boot:run

# Executar testes unitários
mvn test

# Compilar projeto
mvn clean compile
```

## Login com PIN e ID do aparelho (F1-BACK-02)

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
