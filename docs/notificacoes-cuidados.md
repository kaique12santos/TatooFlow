# F2-BACK-04 — Notificações de cuidados pré e pós-sessão (UC08)

O backend executa dois jobs independentes, a cada 60 segundos por padrão:

- **Pré-sessão:** envia para agendamentos `AGENDADO`, a partir de X horas antes do início, enquanto a sessão ainda não começou. Padrão: 24 horas.
- **Pós-sessão:** envia somente para agendamentos `CONCLUIDO`, a partir de X horas após o término previsto (`dataHora + duracaoMinutos`). Padrão: 2 horas. O modelo atual não registra o horário real de conclusão.

Cancelados e sessões em andamento não recebem esses envios. Se o backend ficar indisponível, o próximo ciclo recupera os pré-sessão ainda anteriores ao início e os pós-sessão pendentes, sem prazo de expiração. Agendamentos criados dentro da janela pré recebem no próximo ciclo. Não é necessário salvar novamente um agendamento para entrar na seleção.

O fuso padrão é `America/Sao_Paulo`, independente do fuso do servidor. `dataHora` continua sendo uma data/hora local, interpretada no fuso configurado para as notificações. As verificações começam após o primeiro intervalo; o prazo é uma elegibilidade, com atraso de até um ciclo em condições normais, além do tempo de processamento.

## Configuração

| Propriedade sob `notificacoes.cuidados` | Variável de ambiente | Padrão |
| --- | --- | --- |
| `enabled` | `NOTIFICACOES_CUIDADOS_ENABLED` | `true` |
| `pre-horas` | `NOTIFICACOES_CUIDADOS_PRE_HORAS` | `24` (mínimo 1) |
| `pos-horas` | `NOTIFICACOES_CUIDADOS_POS_HORAS` | `2` (mínimo 0) |
| `intervalo-ms` | `NOTIFICACOES_CUIDADOS_INTERVALO_MS` | `60000` (mínimo 1) |
| `zona` | `NOTIFICACOES_CUIDADOS_ZONA` | `America/Sao_Paulo` |
| `whatsapp-template-pre` | `NOTIFICACOES_CUIDADOS_WHATSAPP_TEMPLATE_PRE` | `cuidados_pre_sessao` |
| `whatsapp-template-pos` | `NOTIFICACOES_CUIDADOS_WHATSAPP_TEMPLATE_POS` | `cuidados_pos_sessao` |
| `whatsapp-idioma` | `NOTIFICACOES_CUIDADOS_WHATSAPP_IDIOMA` | `pt_BR` |
| `template-pre` | `NOTIFICACOES_CUIDADOS_TEMPLATEPRE` (binding Spring) | Texto pré definido em `NotificacaoProperties` |
| `template-pos` | `NOTIFICACOES_CUIDADOS_TEMPLATEPOS` (binding Spring) | Texto pós definido em `NotificacaoProperties` |

As alterações de configuração são aplicadas ao reiniciar o backend. Desabilitar `enabled` remove os dois jobs. Valores inválidos de prazo, intervalo, fuso e templates vazios impedem a inicialização.

## Templates de mensagens

No **mock**, `template-pre` e `template-pos` configuram o texto com as variáveis `{nome}`, `{data}` (`dd/MM/yyyy`) e `{hora}` (`HH:mm`), referentes ao início da sessão. Os padrões remetem às orientações entregues pelo estúdio; personalize os textos com as instruções de cuidados adotadas pelos profissionais.

Exemplo em um arquivo local de propriedades:

```properties
notificacoes.cuidados.pre-horas=12
notificacoes.cuidados.pos-horas=3
notificacoes.cuidados.template-pre=Olá, {nome}! Sua sessão será em {data} às {hora}. Confira as instruções de preparação entregues pelo estúdio.
notificacoes.cuidados.template-pos=Olá, {nome}! Sobre sua sessão de {data} às {hora}: siga as instruções de cuidados entregues pelo tatuador e fale conosco em caso de dúvidas.
```

No **WhatsApp real**, os jobs usam mensagens do tipo `template`, com nome e idioma configuráveis. O conteúdo efetivamente recebido é o template aprovado no WhatsApp Manager, e não o texto do mock. Cadastre dois templates com o conteúdo de cuidados do estúdio e **três parâmetros posicionais no corpo**, nesta ordem:

| Parâmetro Meta | Valor |
| --- | --- |
| `{{1}}` | Nome do cliente |
| `{{2}}` | Data de início da sessão (`dd/MM/yyyy`) |
| `{{3}}` | Hora de início da sessão (`HH:mm`) |

Use templates somente de corpo de texto, sem cabeçalho ou botões com parâmetros. Configure os nomes e o idioma para corresponderem aos aprovados na conta. O formato enviado segue a [referência de templates mantida pela Meta](https://whatsapp.github.io/WhatsApp-Nodejs-SDK/api-reference/types/TemplateObject/). Templates aprovados permitem iniciar a comunicação fora da janela de atendimento, conforme a [documentação de templates do WhatsApp Business](https://whatsappbusiness.com/blog/manage-message-templates-whatsapp-business-api/).

## Envio real e simulação

Para envio real, configure as credenciais da [F2-INFRA-01](whatsapp-sandbox.md) e `WHATSAPP_ENABLED=true`. O cliente envia um POST autenticado a `https://graph.facebook.com/{versao}/{phone-number-id}/messages`, com timeout de conexão de 5 segundos e leitura de 15 segundos. Os testes usam um servidor HTTP simulado, sem acessar a Meta.

Para simular localmente, use `WHATSAPP_ENABLED=false` e `WHATSAPP_MOCK_ENABLED=true` em perfil `h2` ou `dev`. O sender registra o texto renderizado no histórico local, sem chamar a API. Esses envios também geram registros persistidos: use uma base de desenvolvimento separada da produção. Com ambos desabilitados, os cuidados não são marcados como enviados; permanecem pendentes.

Exemplo PowerShell, executado em `backend`:

```powershell
$env:WHATSAPP_ENABLED = 'false'
$env:WHATSAPP_MOCK_ENABLED = 'true'
mvn spring-boot:run '-Dspring-boot.run.profiles=h2'
```

O cliente deve existir na tabela `clientes`, com `telefone` contendo código do país e DDD, e estar vinculado pelo `clienteId` do agendamento. Para testar rapidamente, configure `pos-horas=0` e crie um agendamento `CONCLUIDO` cujo início + duração já tenha passado. Para o pré, crie um `AGENDADO` dentro da janela configurada.

## Persistência e falhas

A migration `V9__criar_notificacao_envios.sql` cria o histórico com unicidade por `(agendamento_id, tipo)` e um índice de seleção por status/data. Cada agendamento recebe no máximo um registro de pré e um de pós; remarcar um agendamento que já recebeu uma notificação não reinicia esse histórico.

Cada envio bloqueia o agendamento no banco, revalida status, prazo e histórico e usa uma transação independente. Isso evita envios concorrentes duplicados em condições normais, inclusive com múltiplas instâncias. Falhas do provedor ou ausência de cliente/telefone não geram registro de sucesso, não interrompem os demais agendamentos e são tentadas novamente no próximo ciclo. Os logs dos jobs identificam somente o agendamento, o tipo e a classe do erro.

O registro indica **aceite da chamada pela API**, não entrega ou leitura pelo cliente. Não há confirmação de entrega via webhook nesta tarefa. Se a Meta aceitar a mensagem e a aplicação cair antes de confirmar a transação, ou se a resposta se perder, uma nova tentativa poderá repetir a mensagem: a chamada externa e a transação do banco não são atômicas.

## Validação automatizada

### Teste pela interface local

Após entrar no frontend, use **Ver agenda** ou **Novo agendamento** na triagem
do administrador, ou a aba **Agenda** do tatuador. A tela permite selecionar ou
cadastrar um cliente, selecionar um tatuador ativo e salvar o agendamento no banco.
Administradores também podem cadastrar um tatuador no mesmo formulário.

Para o pré-sessão, escolha status **Agendado** e um horário futuro dentro da
janela configurada. Para o pós-sessão, escolha status **Concluído** com início e
duração cujo término + prazo já tenha passado. Também é possível usar o botão
**Concluir** em uma sessão da agenda. A data/hora considerada continua sendo o
término previsto, independentemente de quando esse botão for acionado.

Aguarde um ciclo do job e atualize a consulta a `notificacao_envios` no console H2.
O histórico de envios ainda é consultado pelo banco; a agenda mostra o status da
sessão. Consulte [a configuração do frontend](../frontend/README.md).

### Testes do backend

```bash
cd backend
mvn test
```

Os testes verificam registro e desativação dos dois jobs, configuração inválida, limites de horário e duração, templates, status inelegíveis, cancelamento/remarcação após seleção, persistência sem repetição, chamadas concorrentes, falhas isoladas e novas tentativas, além do contrato HTTP de envio de texto/template e do mock sem chamadas externas.
