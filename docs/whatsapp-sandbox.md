# F2-INFRA-01 — Sandbox e credenciais da WhatsApp Business API (Meta)

## Escopo e situação

Esta entrega prepara a configuração local e a validação do webhook de teste.
Não implementa mock de mensagens, parsing de eventos, bot, persistência de triagem,
pagamentos ou notificações das demais tarefas.

| Critério de aceite | Situação nesta entrega |
| --- | --- |
| Conta de desenvolvedor Meta criada | Pendente: cadastro pelo responsável na Meta |
| Número de teste configurado | Pendente: criar aplicativo e obter número no painel Meta |
| Webhook de recebimento validado (ping de teste) | Implementado e coberto por testes locais; validação real no painel pendente |

Os testes automatizados usam valores fictícios e não acessam a Meta. Não são evidência
de que uma conta, um número ou uma assinatura de webhook real já existe.

## 1. Criar a conta e o aplicativo

1. Acesse [Meta for Developers](https://developers.facebook.com/) com a conta do responsável
   e conclua o cadastro de desenvolvedor e as verificações solicitadas.
2. Crie um aplicativo com o caso de uso/produto WhatsApp e associe o portfólio empresarial
   solicitado pelo painel. Os nomes das telas podem variar conforme a conta.
3. Na configuração inicial/API Setup do WhatsApp, obtenha o número de teste, o
   **Phone Number ID** e o **WhatsApp Business Account ID (WABA ID)**.
4. Adicione e verifique um destinatário de teste que pertença à equipe. Use o envio de
   teste disponibilizado no painel para conferir o recebimento no WhatsApp desse destinatário.
5. Obtenha o token de acesso de teste, o App Secret nas configurações do aplicativo e
   a versão da Graph API exibida no painel. Tokens temporários expiram: substitua-os
   quando necessário. Não use o número pessoal de um cliente para estes testes.

O Phone Number ID é um identificador da API, não o número de telefone em formato internacional.

## 2. Configurar o backend sem versionar segredos

Por padrão, `WHATSAPP_ENABLED=false`. O restante da aplicação pode iniciar sem credenciais,
e as rotas GET/POST do webhook retornam HTTP 503 enquanto a integração estiver desabilitada.
Ao habilitar, a aplicação exige todos os campos abaixo; configuração incompleta impede
a inicialização, sem incluir os valores dos segredos na mensagem de validação.

| Variável de ambiente | Propriedade Spring | Origem |
| --- | --- | --- |
| `WHATSAPP_ENABLED` | `integrations.whatsapp.enabled` | `true` para habilitar |
| `WHATSAPP_VERIFY_TOKEN` | `integrations.whatsapp.verify-token` | Token aleatório escolhido pela equipe, igual ao informado no cadastro do webhook |
| `WHATSAPP_APP_SECRET` | `integrations.whatsapp.app-secret` | App Secret do aplicativo Meta; autentica entregas POST |
| `WHATSAPP_ACCESS_TOKEN` | `integrations.whatsapp.access-token` | Token de acesso fornecido pela Meta |
| `WHATSAPP_PHONE_NUMBER_ID` | `integrations.whatsapp.phone-number-id` | ID do número de teste |
| `WHATSAPP_BUSINESS_ACCOUNT_ID` | `integrations.whatsapp.business-account-id` | WABA ID |
| `WHATSAPP_GRAPH_API_VERSION` | `integrations.whatsapp.graph-api-version` | Versão suportada indicada pela Meta, no formato `vNN.N` |

O verify token, o token de acesso e o App Secret são valores distintos. Nesta tarefa,
token de acesso, IDs e versão ficam disponíveis na configuração para conferir o sandbox;
o cliente de envio de mensagens não é implementado.

Alternativa local às variáveis de ambiente, em PowerShell, a partir de `backend/`:

```powershell
Copy-Item -LiteralPath ./whatsapp-local.properties.example -Destination ./whatsapp-local.properties
```

Preencha o arquivo copiado no seu editor. Ele é ignorado pelo Git. O `.example` deve
permanecer sem credenciais. Gere um verify token aleatório, por exemplo:

```powershell
$tokenBytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($tokenBytes)
$rng.Dispose()
[Convert]::ToBase64String($tokenBytes)
```

Inicie o backend carregando explicitamente o arquivo local:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.config.additional-location=optional:file:./whatsapp-local.properties"
```

Para testar apenas a infraestrutura sem MySQL, acrescente o perfil H2:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=h2" "-Dspring-boot.run.arguments=--spring.config.additional-location=optional:file:./whatsapp-local.properties"
```

O Spring não carrega `.env` automaticamente. Em homologação, prefira variáveis de ambiente
ou o gerenciador de segredos do ambiente, com credenciais próprias.

## 3. Conferir o acesso ao número de teste

Opcionalmente, execute a consulta de leitura abaixo em PowerShell. Informe o token somente
no prompt oculto. Copie o ID e a versão do painel; o resultado não imprime credenciais.

```powershell
$waVersion = Read-Host 'Versao Graph API (vNN.N)'
$waPhoneId = Read-Host 'Phone Number ID'
if ($waVersion -notmatch '^v\d+\.\d+$' -or $waPhoneId -notmatch '^\d+$') {
    throw 'Versao ou Phone Number ID invalido'
}
$waToken = Read-Host 'Token de acesso Meta' -AsSecureString
$waCredential = New-Object System.Net.NetworkCredential('', $waToken)
$waHeaders = @{ Authorization = 'Bearer ' + $waCredential.Password }
try {
    $waNumber = Invoke-RestMethod -Method Get -Headers $waHeaders -Uri "https://graph.facebook.com/$waVersion/$waPhoneId`?fields=id,display_phone_number,verified_name"
    $waNumber | Select-Object id, display_phone_number, verified_name
} catch {
    Write-Error 'Nao foi possivel consultar o numero. Confira IDs, permissoes e validade do token no painel Meta.'
} finally {
    Remove-Variable waHeaders, waCredential, waToken -ErrorAction SilentlyContinue
}
```

## 4. Validar o webhook localmente e na Meta

O callback existente é **`/api/whatsapp/webhook`**, com o prefixo `/api`.

### Verificação GET

A Meta envia `hub.mode=subscribe`, `hub.verify_token` e `hub.challenge`.
Com token correto, o backend responde HTTP 200 com o challenge exato em texto puro,
preservando zeros iniciais. Token incorreto/ausente retorna 403; modo inválido ou challenge
ausente retorna 400. Não exige JWT.

Para conferir localmente com o backend habilitado, informe o verify token no prompt oculto:

```powershell
$waVerifyToken = Read-Host 'Verify token configurado no backend' -AsSecureString
$waVerifyCredential = New-Object System.Net.NetworkCredential('', $waVerifyToken)
$waEncodedToken = [Uri]::EscapeDataString($waVerifyCredential.Password)
try {
    $waResult = Invoke-WebRequest -UseBasicParsing -Uri "http://localhost:8080/api/whatsapp/webhook?hub.mode=subscribe&hub.verify_token=$waEncodedToken&hub.challenge=00123456789"
    if ($waResult.StatusCode -ne 200 -or $waResult.Content -cne '00123456789') {
        throw 'Resposta de verificacao inesperada'
    }
    Write-Output 'Handshake local validado: HTTP 200 e challenge exato.'
} finally {
    Remove-Variable waVerifyToken, waVerifyCredential, waEncodedToken -ErrorAction SilentlyContinue
}
```

### Callback HTTPS e ping real

1. Disponibilize o backend por uma URL HTTPS pública com certificado válido, usando um
   ambiente de teste ou um túnel de desenvolvimento aprovado pela equipe. A Meta não
   consegue chamar `localhost` diretamente. Não exponha o console H2; desabilite-o ao
   publicar/tunelar, por exemplo com o argumento `--spring.h2.console.enabled=false`.
2. No painel do aplicativo, cadastre a callback URL
   `https://SEU_HOST/api/whatsapp/webhook` e o mesmo verify token configurado no backend.
3. Execute **Verify and save** (ou ação equivalente) e confirme o registro do callback.
4. Assine o campo `messages` de `whatsapp_business_account` e confirme a assinatura do
   aplicativo na WABA usada para teste, conforme as opções disponíveis no painel.
5. Use **Test** no campo `messages` para disparar o evento de teste. Confirme HTTP 200
   no painel e a mensagem `Evento de teste do webhook WhatsApp autenticado e recebido`
   no log do backend. O evento deve usar o App Secret do mesmo aplicativo.

O POST exige `X-Hub-Signature-256: sha256=<digest>` calculado com HMAC-SHA256 sobre os
bytes originais do corpo e o App Secret. Assinatura ausente, malformada ou incorreta retorna
403. Os logs do código desta integração registram apenas o resultado da verificação,
sem payloads ou segredos. Não habilite DEBUG/TRACE do framework em testes com dados
reais: o Spring pode registrar o corpo das requisições nesse nível.
O evento é somente autenticado e confirmado: ainda não é interpretado, salvo nem respondido
pelo bot. Desative o sandbox quando não estiver em uso; o ACK desta tarefa não processa
mensagens reais de clientes.

## 5. Testes e evidências de aceite

Na pasta `backend/`:

```powershell
mvn "-Dtest=WhatsAppWebhookIntegrationTest,WhatsAppConfigurationTest" test
```

Cobertura: challenge público sem JWT, token incorreto/ausente, modo/challenge inválidos,
assinatura válida com UTF-8, corpo adulterado, assinatura ausente/inválida, configuração
desabilitada, configuração incompleta e manutenção da proteção das demais rotas.

Após o cadastro real, registre no cartão F2-INFRA-01 as evidências sem tokens ou App Secret:

- [ ] Conta de desenvolvedor criada e responsável identificado.
- [ ] Aplicativo, WABA e número de teste disponíveis; envio de teste recebido pelo destinatário.
- [ ] Callback verificado pela Meta e campo `messages` assinado.
- [ ] Ping POST autenticado retornando HTTP 200; data e ambiente do teste registrados.

## Referências oficiais

- [Início da Cloud API](https://developers.facebook.com/docs/whatsapp/cloud-api/get-started)
- [Referência de webhooks da Meta: challenge e assinatura](https://whatsapp.github.io/WhatsApp-Nodejs-SDK/api-reference/webhooks/start/)
- [Coleções da Meta para WhatsApp](https://www.postman.com/meta/whatsapp-business-platform/overview)

A referência do SDK está arquivada; é usada para o protocolo de verificação, sem adicionar
o SDK ao projeto. Confirme telas e versão da Graph API no painel atual da Meta.
