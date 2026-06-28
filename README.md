# 📊 Planilha Reconciliation v2

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://reactjs.org)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.2-3178C6?style=for-the-badge&logo=typescript&logoColor=white)](https://www.typescriptlang.org)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)

Sistema web fullstack para reconciliação mensal de dados de estagiários entre os setores financeiro e cadastro. Evoluído a partir de um sistema desktop (JavaFX) para uma aplicação web completa com back-end Spring Boot, front-end React + TypeScript, autenticação JWT com refresh tokens, processamento assíncrono com progresso em tempo real via SSE e relatório Excel exportável.

---

## 🧩 Sobre o Projeto

O sistema compara duas planilhas Excel — uma do setor **financeiro** e outra do setor de **cadastro** — e identifica automaticamente:

- Registros **conformes** (presentes e iguais nos dois lados)
- Registros **faltantes** no cadastro
- Registros **excedentes** no cadastro
- **Divergências** de campos (matrícula, CPF, nome, datas)
- **Possíveis abreviações** de nomes (detecção por similaridade)
- Registros **cancelados**

O resultado é apresentado visualmente via gráficos (Recharts) e pode ser exportado como relatório Excel multi-abas.

---

## 🚀 Funcionalidades

### 🔐 Autenticação e Segurança
- Login com JWT stateless (access token de 15 min + refresh token de 7 dias)
- Renovação automática de token via `/api/auth/refresh`
- Senhas criptografadas com BCrypt
- Rate limiting no endpoint de login: 5 tentativas por minuto por IP (Bucket4j)
- RBAC com dois papéis: `ADMIN` e `OPERATOR`
- Auditoria de ações em tabela dedicada (`audit_log`)

### 📊 Reconciliação
- Upload de duas planilhas `.xlsx` via formulário
- Normalização automática de CPF (remove pontuação, completa zeros à esquerda), nomes (lowercase, sem acentos) e datas
- Detecção de possíveis abreviações de nomes por similaridade
- Filtro por intervalo de data de início do estágio
- Processamento **assíncrono** com retorno imediato de `sessionId`
- Progresso em tempo real via **Server-Sent Events (SSE)**

### 📈 Visualização e Relatórios
- Dashboard com gráficos de pizza e barras (Recharts)
- Histórico paginado de reconciliações anteriores com filtros por data e usuário
- Download do relatório Excel gerado pelo back-end

### 🛠️ Infraestrutura
- Containerização completa com **Docker Compose** (backend, frontend, PostgreSQL, Nginx)
- Nginx como reverse proxy entre front-end e back-end
- Migrations de banco com **Flyway**
- Testes com **Testcontainers** (PostgreSQL real nos testes de repositório)

---

## 🏗️ Arquitetura

```
planilha-reconciliation-v2/
├── backend/                  # Spring Boot 3 (Java 17)
│   └── src/
│       ├── controller/       # Endpoints REST (Auth, Reconciliation, History, Report)
│       ├── service/
│       │   ├── comparador/   # Lógica de comparação e detecção de divergências
│       │   ├── reader/       # Leitura e parsing das planilhas Excel (Apache POI)
│       │   └── relatorio/    # Geração do relatório Excel de saída
│       ├── security/         # JWT, RateLimiting, SecurityConfig
│       ├── model/            # Entidades JPA (User, ReconciliationHistory, AuditLog)
│       └── resources/
│           └── db/migration/ # Scripts Flyway (V1, V2)
├── frontend/                 # React 18 + TypeScript + Vite + TailwindCSS
│   └── src/
│       ├── pages/            # Login, Dashboard, History, HistoryDetail
│       ├── components/       # ResultCharts (Recharts), ProtectedRoute
│       ├── hooks/            # useReconciliationProgress (SSE)
│       ├── services/         # api.ts (Axios), authService, reconciliationService, historyService
│       └── contexts/         # AuthContext (estado global de autenticação)
├── nginx/
│   └── nginx.conf            # Configuração do reverse proxy
└── docker-compose.yml
```

---

## 🛠️ Tecnologias

| Camada | Tecnologias |
|---|---|
| **Back-end** | Java 17, Spring Boot 3.2.5, Spring Security 6, Spring Data JPA |
| **Banco de Dados** | PostgreSQL 15, Hibernate, Flyway |
| **Autenticação** | JWT (jjwt 0.12.5), BCrypt, Bucket4j 8.10 (rate limiting) |
| **Processamento** | Apache POI 5.2.5 (Excel), Commons Lang3, Commons IO |
| **Tempo real** | Server-Sent Events (SSE) via Spring `SseEmitter` |
| **Front-end** | React 18, TypeScript 5.2, Vite, TailwindCSS 3 |
| **UI / Gráficos** | Recharts, React Router 6, React Hook Form + Zod |
| **HTTP Client** | Axios, TanStack React Query v5 |
| **Testes** | JUnit 5, Mockito, Spring Security Test, Testcontainers |
| **Infra** | Docker, Docker Compose, Nginx |

---

## ⚙️ Como Executar

### Pré-requisitos
- Docker e Docker Compose instalados

### Subir todos os serviços

```bash
git clone https://github.com/DouglasLira-Dev/planilha-reconciliation-v2.git
cd planilha-reconciliation-v2

docker-compose up -d
```

Após o build:

| Serviço | URL |
|---|---|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| PostgreSQL | localhost:5432 |

### Variáveis de Ambiente (back-end)

| Variável | Descrição | Padrão |
|---|---|---|
| `SPRING_DATASOURCE_URL` | URL do banco PostgreSQL | `jdbc:postgresql://postgres:5432/reconciliation` |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco | `admin` |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco | `admin123` |
| `JWT_SECRET` | Chave secreta para JWT (mín. 32 chars) | valor padrão inseguro |

> ⚠️ Em produção, substitua todas as variáveis com valores seguros via arquivo `.env` ou secrets do seu provedor.

### Executar apenas o back-end (sem Docker)

```bash
cd backend
mvn clean spring-boot:run
```

### Executar os testes

```bash
cd backend
mvn test
```

---

## 📮 Endpoints da API

### 🔐 Autenticação

| Método | Endpoint | Descrição | Auth |
|---|---|---|---|
| POST | `/api/auth/login` | Login e geração de tokens | Público |
| POST | `/api/auth/refresh` | Renovar access token | Público |
| POST | `/api/auth/logout` | Logout (stateless) | Público |

### 📊 Reconciliação

| Método | Endpoint | Descrição | Auth |
|---|---|---|---|
| POST | `/api/reconcile` | Reconciliação síncrona | JWT |
| POST | `/api/reconcile/async` | Inicia processamento assíncrono, retorna `sessionId` | JWT |
| GET | `/api/reconcile/progress/{sessionId}` | Stream de progresso via SSE | JWT |

### 📁 Histórico

| Método | Endpoint | Descrição | Auth |
|---|---|---|---|
| GET | `/api/history` | Lista histórico paginado (filtros por data e usuário) | JWT |
| GET | `/api/history/{id}` | Detalhes de uma reconciliação | JWT |
| DELETE | `/api/history/{id}` | Remove um histórico | ADMIN |

### 📄 Relatório

| Método | Endpoint | Descrição | Auth |
|---|---|---|---|
| GET | `/api/report/{historyId}` | Download do relatório Excel gerado | JWT |

---

## 🧪 Testes

14 testes no total, cobrindo as camadas mais críticas:

| Classe | Testes | Cobertura |
|---|---|---|
| `NormalizacaoUtilTest` | 7 | Normalização de CPF, nome, datas e strings |
| `UserRepositoryTest` | 3 | Queries JPA com Testcontainers (PostgreSQL real) |
| `ComparadorPlanilhasServiceTest` | 3 | Lógica de comparação: conforme, faltante, excedente |
| `AuthControllerTest` | 1 | Endpoint de login com MockMvc + Spring Security |

---

## 🔄 Fluxo de Reconciliação Assíncrona

```
Cliente → POST /api/reconcile/async (upload dos dois arquivos)
       ← sessionId (UUID)

Cliente → GET /api/reconcile/progress/{sessionId}  (abre conexão SSE)
       ← event: progress { percentage: 10, message: "Lendo planilha financeira..." }
       ← event: progress { percentage: 50, message: "Comparando registros..." }
       ← event: progress { percentage: 90, message: "Gerando relatório Excel..." }
       ← event: complete { resultado completo com totais e divergências }
```

---

## 📝 Licença

Este projeto está sob a licença MIT. Consulte o arquivo [LICENSE](LICENSE) para mais detalhes.

---

**Desenvolvido por [Douglas Lira](https://github.com/DouglasLira-Dev)**

[![LinkedIn](https://img.shields.io/badge/LinkedIn-Douglas%20Lira-0077B5?style=flat&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/dev-douglas-lira/)