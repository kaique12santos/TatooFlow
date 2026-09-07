# TattooFlow — Sistema de Gestão para Estúdios de Tatuagem

**TattooFlow** é uma solução completa para gerenciamento de estúdios de tatuagem, otimizando o fluxo de trabalho de tatuadores e administradores, desde a triagem de clientes até o controle financeiro e estoque.

---

## 🏗 Arquitetura do Monorepo

```
tattooflow/
├── backend/                     # Spring Boot (Java 17) - MVC por Domínio
├── frontend/                    # React Native - Mobile
├── docs/                        # Documentação Técnica e Acadêmica
├── .github/                     # Workflows CI/CD e PR Template
├── docker-compose.yml           # MySQL 8 e Serviços Locais
└── README.md
```

---

## 🚀 Como Executar o Projeto Localmente

### Pré-requisitos
- **Java 17+** & **Maven 3.8+**
- **Node.js 18+** & **npm** (ou Yarn)
- **Docker** & **Docker Compose**

---

### 1. Subindo o Banco de Dados (MySQL)

```bash
docker-compose up -d
```

---

### 2. Executando o Backend (Spring Boot)

```bash
cd backend
mvn spring-boot:run
```

O backend estará acessível em `http://localhost:8080`.
Documentação da API (Swagger UI): `http://localhost:8080/swagger-ui.html`

---

### 3. Executando o Frontend (React Native / Expo)

```bash
cd frontend
npm install
npm start
```

---

## 🛠 Tecnologias Utilizadas

- **Backend**: Java 17, Spring Boot, Spring Security (JWT), Flyway, MySQL, Swagger.
- **Frontend**: React Native, TypeScript, Axios, React Navigation.
- **Integrações**: WhatsApp Webhook/Client, Google Gemini AI, Instagram Graph API.
