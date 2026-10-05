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


## Consultar agendamentos salvos

`GET /api/agendamentos` consulta a tabela `agendamentos` do banco configurado no perfil ativo e retorna todos os registros, incluindo os cancelados. A lista fica no campo `data` da resposta. Quando nao ha registros, retorna HTTP 200 com `data: []`.

A rota exige o cabecalho `Authorization: Bearer <token>`. No PowerShell, obtenha o token pela rota de login e consulte os agendamentos:

```powershell
$login = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/auth/login" -Body @{ email = "seu@email.com"; senha = "sua-senha" }
$headers = @{ Authorization = "Bearer $($login.data)" }
$resposta = Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/agendamentos" -Headers $headers
$resposta.data
```

Para consultar somente os registros de um tatuador, use `GET /api/agendamentos/tatuador/{tatuadorId}` com o mesmo cabecalho.

### Usar JWT no Swagger

1. Com o backend em execução, abra `http://localhost:8080/swagger-ui/index.html`.
2. Expanda `POST /api/auth/login`, clique em **Try it out**, preencha os parâmetros `email` e `senha` e clique em **Execute**. Esse endpoint recebe parâmetros, não um corpo JSON.
3. Copie somente o token do campo `data` da resposta, sem as aspas.
4. Clique em **Authorize**, cole o token no campo de `bearerAuth` **sem escrever `Bearer `** e confirme em **Authorize**. Depois clique em **Close**.
5. Expanda `GET /api/agendamentos`, clique em **Try it out** e depois em **Execute**. O Swagger envia automaticamente `Authorization: Bearer <token>` e a lista aparece em `data`.

A mesma autorização funciona em `GET /api/agendamentos/tatuador/{tatuadorId}` e `POST /api/agendamentos`. O login permanece disponível sem token.

Se o token expirar ou o backend reiniciar, faça login novamente e substitua o token em **Authorize**. O JWT atual dura 24 horas e usa uma chave gerada a cada inicialização. Sem um JWT válido, as rotas de agendamentos retornam HTTP 403 na configuração atual.

O serviço de login atual gera o JWT a partir do e-mail informado, mas ainda não valida a senha.

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
