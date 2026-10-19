# F2-INFRA-02 — Mock local do WhatsApp

O mock simula o recebimento de mensagens de texto sem conta Meta, número de teste,
token, App Secret, acesso à internet ou assinatura de webhook. É uma alternativa explícita
para desenvolvimento: não há troca automática para mock quando a API real falha.

## Iniciar localmente

Na pasta `backend/`, use H2 em memória e habilite somente o mock:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=h2" "-Dspring-boot.run.arguments=--integrations.whatsapp.mock.enabled=true --integrations.whatsapp.enabled=false --server.address=127.0.0.1"
```

Este comando funciona sem configurar o arquivo de credenciais da F2-INFRA-01.
O argumento `--server.address=127.0.0.1` mantém este teste acessível pela máquina local.
O banco H2 existe enquanto a aplicação estiver em execução e é usado pelo restante
do backend; o mock não salva mensagens nele.

Alternativa com variáveis de ambiente, na mesma sessão PowerShell:

```powershell
$env:WHATSAPP_MOCK_ENABLED = 'true'
$env:WHATSAPP_ENABLED = 'false'
mvn spring-boot:run "-Dspring-boot.run.profiles=h2" "-Dspring-boot.run.arguments=--server.address=127.0.0.1"
```

Também é possível usar o perfil `dev`, com o MySQL já configurado. O mock só é registrado
nos perfis `dev` ou `h2`, com a flag habilitada, e nunca quando `prod` está ativo, inclusive
com perfis combinados. Por padrão, `WHATSAPP_MOCK_ENABLED=false`.

## Simular uma mensagem recebida

**POST `http://localhost:8080/api/whatsapp/mock/messages`**

O endpoint local não exige JWT nem assinatura Meta. Ele recebe JSON:

```json
{
  "telefone": "5511999999999",
  "texto": "Quero uma tatuagem de flor no braço."
}
```

| Campo | Validação |
| --- | --- |
| `telefone` | Obrigatório; 8 a 15 dígitos, incluindo DDI, sem `+`, espaços ou pontuação e sem zero inicial |
| `texto` | Obrigatório; não pode conter apenas espaços; máximo de 4096 caracteres |

Em outro terminal PowerShell:

```powershell
$mockBody = @{
    telefone = '5511999999999'
    texto = 'Quero uma tatuagem de flor no braço.'
} | ConvertTo-Json

$mockResponse = Invoke-RestMethod -Method Post `
    -Uri 'http://localhost:8080/api/whatsapp/mock/messages' `
    -ContentType 'application/json; charset=utf-8' `
    -Body ([System.Text.Encoding]::UTF8.GetBytes($mockBody))

$mockResponse | ConvertTo-Json -Depth 15
```

Resposta HTTP 200, em formato resumido:

```json
{
  "success": true,
  "message": "Mensagem recebida em modo MOCK (simulacao local)",
  "data": {
    "source": "MOCK",
    "messageId": "wamid.mock.<uuid>",
    "telefone": "5511999999999",
    "texto": "Quero uma tatuagem de flor no braço.",
    "receivedAt": "<instante UTC>",
    "payload": {
      "object": "whatsapp_business_account",
      "entry": [
        {
          "id": "mock-waba-id",
          "changes": [
            {
              "field": "messages",
              "value": {
                "messaging_product": "whatsapp",
                "metadata": {
                  "display_phone_number": "5500000000000",
                  "phone_number_id": "mock-phone-number-id"
                },
                "contacts": [
                  {
                    "wa_id": "5511999999999",
                    "profile": { "name": "Cliente de teste" }
                  }
                ],
                "messages": [
                  {
                    "from": "5511999999999",
                    "id": "wamid.mock.<uuid>",
                    "timestamp": "<segundos Unix>",
                    "type": "text",
                    "text": { "body": "Quero uma tatuagem de flor no braço." }
                  }
                ]
              }
            }
          ]
        }
      ]
    }
  },
  "timestamp": "<instante da resposta>"
}
```

O payload gerado usa a estrutura de um evento de texto recebido, com IDs fictícios
e timestamp em segundos Unix. Cada chamada gera um ID de mensagem distinto.
A resposta identifica explicitamente a origem `MOCK`.

## O que acontece no backend

1. O controller valida telefone, texto e tipo de conteúdo.
2. `WhatsAppMockService` gera o payload local.
3. Publica `WhatsAppMockMessageReceivedEvent` pelo mecanismo de eventos do Spring.
4. Registra o ID da simulação no log e devolve o evento na resposta.

O evento permite conectar e testar futuros consumidores internos com
`@EventListener(WhatsAppMockMessageReceivedEvent.class)`. Nesta entrega não há consumidor
de triagem: HTTP 200 confirma apenas a simulação e a publicação do evento, não a criação
de um cliente, pré-agendamento ou resposta do bot. Não há armazenamento nem histórico
das mensagens; o evento é entregue aos listeners presentes naquele processo.

Não há chamada HTTP à Meta. O endpoint real `/api/whatsapp/webhook` mantém a verificação
de assinatura da F2-INFRA-01 e continua retornando 503 se a integração real estiver
desabilitada. O mock não contorna essa validação.

| HTTP | Significado |
| --- | --- |
| 200 | Mensagem simulada e evento local publicado |
| 400 | JSON inválido, corpo ausente ou campos fora do contrato |
| 415 | Content-Type diferente de `application/json` |

Se a flag estiver desabilitada ou o perfil não for permitido, o controller não é registrado.
O endpoint não ficará disponível; não interprete um 403/404 nessa situação como erro da Meta.

## Desabilitar e testar

Pare o backend e reinicie sem a flag, ou com `--integrations.whatsapp.mock.enabled=false`.
Se usou variáveis de ambiente, remova a ativação da sessão antes de reiniciar:

```powershell
Remove-Item Env:WHATSAPP_MOCK_ENABLED -ErrorAction SilentlyContinue
```

Execute os testes na pasta `backend/`:

```powershell
mvn "-Dtest=WhatsAppMockIntegrationTest,WhatsAppMockAvailabilityTest" test
```

Os testes conferem recebimento sem Meta/JWT, publicação de evento, caracteres UTF-8,
IDs distintos, limites e erros de entrada, ausência de chamadas ao cliente oficial,
proteção das demais rotas e bloqueio do mock por perfil/flag, incluindo `prod` combinado
com um perfil local.

## Critérios de aceite

- [x] Endpoint de simulação de mensagem recebida.
- [x] Documentação de como usar o mock localmente.

A implementação se limita à F2-INFRA-02. Parsing de eventos reais, coleta de respostas
do bot, persistência e pagamentos permanecem nas suas tarefas específicas.

Referência da estrutura de texto: [Messages Object — coleção oficial da Meta](https://www.postman.com/meta/whatsapp-business-platform/folder/1dtuocp/messages-object).
