# frontend

## Agendamentos integrados ao backend

Com o backend em execução, configure `VITE_API_BASE_URL=http://127.0.0.1:8080`
em `.env.local` e inicie `npm run dev`. O login usa o PIN e o ID do aparelho
cadastrado para cada conta; não é necessário fixar um aparelho no frontend.

O perfil `local` do backend usa o banco persistente `backend/data/tattooflow.mv.db`.
As credenciais locais criadas ficam em `.local/accounts.json`, fora do Git.
Após a primeira criação, inicie o backend com:

```powershell
cd backend
java -jar target/tattooflow-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

Para criar as duas contas em um banco local novo, habilite
`--app.local-accounts.enabled=true` e defina `TATTOOFLOW_ADMIN_PIN` e
`TATTOOFLOW_TATUADOR_PIN` no ambiente. A inicialização preserva contas existentes.

Após entrar, abra **Ver agenda** ou **Novo agendamento** na triagem do administrador.
O perfil de tatuador acessa a mesma agenda pela aba **Agenda**.

O formulário `/agendamento-sessao` permite selecionar cliente e tatuador ativo,
informar data, horário, duração, descrição, valor estimado obrigatório e status.
O valor estimado deve ser maior que zero, com no máximo duas casas decimais.
É possível cadastrar um cliente no próprio formulário; administradores também
podem cadastrar um tatuador, informando e-mail, PIN e aparelho autorizado.
O registro só é confirmado após o sucesso de `POST /api/agendamentos`.

A agenda `/agenda` consulta os registros persistidos, mostra lista vazia quando
não há sessões e permite buscar, filtrar por data/status, iniciar, concluir e
cancelar. As alterações usam o contrato existente de `POST /api/agendamentos`
com o `id` da sessão. Requisições usam o JWT do login; conflitos e falhas são
exibidos sem substituir os resultados por dados simulados.
Agendamentos confirmados possuem o botão **Ver sinal**, com acesso autenticado
ao comprovante. A agenda também oferece **Retornar para triagem**, e as duas
telas compartilham o mesmo componente de filtros de status.

A triagem consulta clientes, pré-agendamentos, agendamentos, tatuadores e
pagamentos reais da API. Não há clientes de exemplo ou confirmação local de
pagamento. Uma lista vazia permanece vazia; falhas são exibidas com opção de
tentar novamente. O sinal exige um arquivo real e usa
`POST /api/agendamentos/{id}/sinal` com multipart e JWT. O comprovante pode ser
consultado tanto na triagem quanto na agenda. Dados ausentes, inclusive respostas
de anamnese, são apresentados como não informados.

As novas sessões são criadas como **Agendado**, confirmadas após o sinal de 50%
e então podem ser iniciadas ou concluídas pela agenda.

Para testar cuidados, cadastre um **Agendado** com início dentro das próximas
24 horas. Para o pós-sessão com a configuração padrão, cadastre uma sessão
**Concluída** cujo início + duração tenha terminado há pelo menos 2 horas.
O job verifica as sessões a cada 60 segundos. Com WhatsApp simulado, confira o
registro na tabela `notificacao_envios` pelo console H2; não há envio ao telefone.
Veja [os prazos e templates configuráveis](../docs/notificacoes-cuidados.md).

This template should help get you started developing with Vue 3 in Vite.

## Recommended IDE Setup

[VS Code](https://code.visualstudio.com/) + [Vue (Official)](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur).

## Recommended Browser Setup

- Chromium-based browsers (Chrome, Edge, Brave, etc.):
  - [Vue.js devtools](https://chromewebstore.google.com/detail/vuejs-devtools/nhdogjmejiglipccpnnnanhbledajbpd)
  - [Turn on Custom Object Formatter in Chrome DevTools](http://bit.ly/object-formatters)
- Firefox:
  - [Vue.js devtools](https://addons.mozilla.org/en-US/firefox/addon/vue-js-devtools/)
  - [Turn on Custom Object Formatter in Firefox DevTools](https://fxdx.dev/firefox-devtools-custom-object-formatters/)

## Type Support for `.vue` Imports in TS

TypeScript cannot handle type information for `.vue` imports by default, so we replace the `tsc` CLI with `vue-tsc` for type checking. In editors, we need [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) to make the TypeScript language service aware of `.vue` types.

## Customize configuration

See [Vite Configuration Reference](https://vite.dev/config/).

## Project Setup

```sh
bun install
```

### Compile and Hot-Reload for Development

```sh
bun dev
```

### Type-Check, Compile and Minify for Production

```sh
bun run build
```

### Lint with [ESLint](https://eslint.org/)

```sh
bun lint
```
