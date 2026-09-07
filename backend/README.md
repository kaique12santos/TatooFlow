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
