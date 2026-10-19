# Política de Segurança - TattooFlow

A segurança das informações de clientes, estúdios, dados financeiros e fichas de procedimentos é uma prioridade fundamental no **TattooFlow**. Agradecemos a colaboração da comunidade de segurança e de pesquisadores na identificação e correção de possíveis vulnerabilidades.

---

## Como Reportar Vulnerabilidades

Se você identificou uma vulnerabilidade de segurança, envie um e-mail para **seguranca@tattooflow.com.br** contendo:

- Descrição detalhada da vulnerabilidade.
- Passos para reprodução (PoC, scripts ou payloads utilizados).
- Comportamento esperado vs. comportamento observado.
- Avaliação de impacto potencial (ex: vazamento de dados de clientes, bypass de autenticação, escalonamento de privilégios).
- Componente ou serviço afetado (ex: API Backend Spring Boot, App Mobile React Native, Webhooks).

Responderemos confirmando o recebimento em até **48 horas** e forneceremos uma avaliação inicial em até **7 dias**.

> [!IMPORTANT]
> **Não abra issues públicas no GitHub para relatar falhas ou vulnerabilidades de segurança.** Utilize sempre o canal privado indicado acima.

---

## Processo de Divulgação Responsável (Responsible Disclosure)

1. **Notificação Privada:** O pesquisador relata a falha pelo e-mail informado.
2. **Confirmação:** Nossa equipe confirma o recebimento em até 48 horas com um identificador de acompanhamento.
3. **Triagem e Análise:** Avaliação de gravidade e prazo estimado de correção em até 7 dias.
4. **Desenvolvimento e Testes:** A correção é desenvolvida em ambiente privado e testada contra regressões e conformidade.
5. **Publicação e Créditos:** Após o deploy da correção, publicamos uma nota de segurança e reconhecemos a contribuição do relator (salvo se solicitado anonimato).
6. **Janela de 90 Dias:** Caso a vulnerabilidade não seja tratada em até 90 dias, o pesquisador poderá divulgá-la publicamente de acordo com as boas práticas do setor.

Pedimos que nenhuma vulnerabilidade seja divulgada publicamente antes que a correção esteja disponível.

---

## Escopo

### Dentro do Escopo (In-Scope):

- **Backend API (Spring Boot):**
  - Falhas de autenticação e gestão de sessão (JWT, tokens expirados ou forjados).
  - Controle de acesso e autorização inadequados (IDOR, escalonamento de privilégios entre Cliente, Tatuador e Administrador).
  - Injeção de código/SQL, bypass de regras de negócio financeiras ou manipulação de estoque e agendamentos.
  - Exposição indevida de dados sensíveis (dados pessoais, anamnese, relatórios financeiros).
  - Tratamento inseguro de webhooks e integrações externas (WhatsApp, Instagram Graph API, Google Gemini AI).
- **Frontend Mobile (React Native):**
  - Armazenamento inseguro de credenciais, tokens ou chaves criptográficas no dispositivo.
  - Vulnerabilidades na comunicação cliente-servidor (falhas de validação SSL/TLS, man-in-the-middle).
- **Infraestrutura e Configuração:**
  - Exposição de credenciais ou variáveis de ambiente confidenciais no repositório.
  - Endpoints de documentação (ex: Swagger/OpenAPI) ou atuadores do Spring que exponham dados operacionais críticos em produção.

### Fora do Escopo (Out-of-Scope):

- Ataques de negação de serviço volumétricos (DDoS).
- Engenharia social (phishing, vishing) contra administradores, tatuadores ou usuários do estúdio.
- Falhas em bibliotecas de terceiros sem um vetor de ataque ou impacto comprovado na aplicação TattooFlow.
- Relatórios gerados exclusivamente por scanners automatizados sem prova de conceito (PoC) ou validação de impacto real.
- Falhas que exijam acesso físico a dispositivos móveis rooteados ou com jailbreak sem mitigações práticas aplicáveis.

---

## Proteção ao Pesquisador (Safe Harbor)

Não iniciaremos nem apoiaremos ações legais contra pesquisadores que ajam de boa-fé e cumpram as seguintes diretrizes:

- Fazer esforços sinceros para evitar violação de privacidade, destruição ou corrupção de dados e degradação de serviços.
- Acessar apenas a quantidade mínima de dados estritamente necessária para demonstrar a existência da falha, sem retenção, vazamento ou compartilhamento de informações.
- Reportar com prontidão e sigilo pelo e-mail oficial, concedendo um prazo razoável para mitigação antes de qualquer divulgação externa.
- Não explorar a vulnerabilidade além do estritamente necessário para sua comprovação (sem persistência, movimentação lateral ou invasão a outros sistemas).

Atividades realizadas em estrita conformidade com esta política são consideradas autorizadas. Esta política de *Safe Harbor* não se aplica a atos ilegais ou que causem danos deliberados a terceiros.
