[![License](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

<p align="center">
  <a href="https://alistair.cockburn.us/hexagonal-architecture/"><img src="https://img.shields.io/badge/Architecture-Hexagonal-brightgreen?style=for-the-badge" alt="Hexagonal Architecture"/></a>
  <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/Spring%20Boot-4.0-brightgreen?style=for-the-badge" alt="Spring Boot 4.0"/></a>
  <a href="https://www.graalvm.org/"><img src="https://img.shields.io/badge/GraalVM-Native-005571?style=for-the-badge" alt="GraalVM Native"/></a>
  <a href="https://graphql.org/"><img src="https://img.shields.io/badge/API-GraphQL-E10098?style=for-the-badge" alt="GraphQL"/></a>
  <a href="https://spring.io/projects/spring-shell"><img src="https://img.shields.io/badge/CLI-Spring%20Shell-6DB33F?style=for-the-badge" alt="Spring Shell"/></a>
  <a href="https://spring.io/projects/spring-ai"><img src="https://img.shields.io/badge/AI-Spring%20AI-6DB33F?style=for-the-badge" alt="Spring AI"/></a>
</p>

---

**Alexandra Job Tracker** is a multi-user job application tracking system with a GraphQL API and CLI
client. Built with hexagonal (ports & adapters) architecture on Java 25, Spring Boot 4, and Maven
multi-module.

---

## Architecture

### Modules

| Module                         | Description                                                           |
|--------------------------------|-----------------------------------------------------------------------|
| `domain`                       | Pure Java — no framework dependencies. Domain models, ports, services |
| `application`                  | Use case implementations orchestrating domain logic through ports     |
| `adapter-api`                  | GraphQL schema, resolvers, DTOs — Spring for GraphQL                  |
| `adapter-cli`                  | Spring Shell commands, GraphQL HTTP client, session management        |
| `infrastructure-persistence`   | JPA entities, repositories, Flyway migrations                         |
| `infrastructure-auth`          | JWT provider, GraphQL auth interceptor                                |
| `infrastructure-ai`            | Job analysis via DeepSeek LLM (Spring AI)                             |
| `infrastructure-cache`         | Caffeine cache with hexagonal `CachePort` decorators                  |
| `infrastructure-observability` | OpenTelemetry, Prometheus, Micrometer                                 |
| `bootstrap-server`             | Spring Boot application — GraphQL API, JPA, Flyway, AI                |
| `bootstrap-cli`                | Spring Boot application — Spring Shell CLI client                     |
| `coverage-jacoco`              | JaCoCo coverage report aggregation + ArchUnit architecture tests      |
| `browser-extension`            | Chrome extension for capturing LinkedIn/Indeed job postings           |

### Layer Constraints

```mermaid
flowchart LR
    domain --> application --> infrastructure
    domain --> application --> adapter
    infrastructure & adapter --> bootstrap
    subgraph infrastructure [infrastructure-*]
        direction LR
        I1[persistence]
        I2[auth]
        I3[ai]
        I4[cache]
        I5[observability]
    end
    subgraph adapter [adapter-*]
        direction LR
        A1[api]
        A2[cli]
    end
    subgraph bootstrap [bootstrap-*]
        direction LR
        B1[server]
        B2[cli]
    end
```

- `domain` is pure Java — zero framework imports. Validated by ArchUnit (13 rules).
- `adapter-*` and `infrastructure-*` depend on `domain` and `application`.
- `bootstrap-*` is the composition root — depends on everything, wires adapters.

### CQRS Resolver Architecture

Resolvers follow **Command Query Responsibility Segregation** — every endpoint is either a
`*QueryResolver` (read) or a `*MutationResolver` (write), never both.

| Query Resolver            | Endpoints                               |
|---------------------------|-----------------------------------------|
| `UserQueryResolver`       | `me`                                    |
| `ApplicationQueryResolver`| `applications`                          |
| `JobPostingQueryResolver` | `jobPostings`                           |
| `AnalyticsQueryResolver`  | `analytics`, `perStatus`                |

| Mutation Resolver              | Endpoints                                               |
|--------------------------------|---------------------------------------------------------|
| `UserMutationResolver`         | `register`, `login`, `logout`                           |
| `ApplicationMutationResolver`  | `createApplication`, `updateApplicationStatus`, `delete` |
| `JobPostingMutationResolver`   | `submitJobPosting`, `analyzeJobPosting`                 |

### Data Flow

```mermaid
flowchart LR
    subgraph inbound [Inbound Adapters]
        direction LR
        CLI["Spring Shell"]
        HTTP["HTTP /graphql"]
    end
    subgraph app [Application]
        direction LR
        QR["*QueryResolver"]
        MR["*MutationResolver"]
        UC["Use Case"]
    end
    subgraph outbound [Outbound Adapters]
        direction LR
        JPA["JPA Adapter"]
        AI["AI Adapter"]
        JWT["JWT Provider"]
    end
    subgraph storage [(Storage)]
        H2[(H2)]
    end

    CLI --> HTTP
    HTTP --> QR & MR
    QR & MR --> UC
    UC --> |inbound Port| JPA & AI & JWT
    JPA --> DB
```

---

## Quick Start

```bash
# Prerequisites: JDK 25, Maven 3.9+
git clone https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker.git
cd alexandra-job-tracker
```

### Build

```bash
mvn compile -pl bootstrap-server -am              # Compile server + deps
mvn test -pl domain                               # Domain unit tests
mvn test -pl coverage-jacoco -am                  # Architecture tests (13 ArchUnit rules)
mvn verify                                        # Full CI (unit + E2E + arch)
mvn package -pl bootstrap-server -am -DskipTests  # Server fat JAR
mvn package -pl bootstrap-cli -am -DskipTests     # CLI fat JAR
```

### Native Compilation (GraalVM)

```bash
mvn install -DskipTests
mvn -Pnative native:compile -pl bootstrap-server -DskipTests  # Server native binary
mvn -Pnative native:compile -pl bootstrap-cli -DskipTests     # CLI native binary
```

### Run

```bash
# Start server with Docker Compose (+telemetry +monitoring)
docker compose -f deploy/compose/docker-compose.yml up -d
```

```bash
# Start server standalone (with embedded H2, no telemetry)
java -jar bootstrap-server/target/bootstrap-server-*.jar
```

```bash
# Start CLI (connects to server)
java -jar bootstrap-cli/target/bootstrap-cli-*.jar --server.url=http://localhost:8880/api
```

```bash
# Start server native (with embedded H2, no telemetry)
./bootstrap-server/target/bootstrap-server-*-native --spring.profiles.active=loc
``` 

```bash

# Start native CLI (connects to server)
./bootstrap-cli/target/bootstrap-cli-*-native --server.url=http://localhost:8880
```

---

## API

The server exposes a GraphQL endpoint at `POST /api/graphql` with the following schema:

| Operation                                       | Description                                      |
|-------------------------------------------------|--------------------------------------------------|
| `register(username, password)`                    | Create account, returns JWT                        |
| `login(username, password)`                       | Authenticate, returns JWT                          |
| `logout`                                          | Invalidate current session                         |
| `me`                                              | Current user info                                  |
| `applications(status, source)`                    | List job applications with optional filters        |
| `createApplication(company, role, source, ...)`   | Track a new application                            |
| `updateApplicationStatus(id, status)`             | Move through pipeline                              |
| `deleteApplication(id)`                           | Remove an application                              |
| `analytics(since)`                                | Aggregated per-status counts and conversion rate   |
| `jobPostings(source)`                             | List scraped job postings                          |
| `submitJobPosting(input)`                         | Submit a job posting from raw data                 |
| `analyzeJobPosting(jobPostingId)`                 | AI-powered job description analysis                |

### Status Pipeline

```mermaid
flowchart LR
    SAVED --> APPLIED
    APPLIED --> INTERVIEWING
    INTERVIEWING --> OFFER
    OFFER --> ACCEPTED
    INTERVIEWING --> REJECTED
    OFFER --> WITHDRAWN
```

---

## CLI Commands

The CLI client connects to the GraphQL API via HTTP. Available commands:

| Command              | Keys          | Description                                   |
|----------------------|---------------|-----------------------------------------------|
| `register`           | `reg`         | Create account                                |
| `login`              | `li`          | Authenticate, stores session token            |
| `logout`             | `lo`          | Invalidate session                            |
| `whoami`             | `who`         | Show current user                             |
| `add`                | `a`           | Track a new job application                   |
| `list`               | `l`           | List job applications                         |
| `update`             | `u`           | Update application status                     |
| `delete`             | `d`           | Remove an application                         |
| `analytics`          | `an`          | Per-status counts and conversion rate         |
| `submit-job`         | `sj`          | Submit a job posting from URL or manual entry |
| `postings`           | `po`          | List scraped job postings                     |
| `analyze`            | `anlz`        | AI analysis of a job posting                  |

```bash
# Examples
java -jar bootstrap-cli/target/bootstrap-cli-*.jar --server.url=http://localhost:8080
register --username alice --password secret
login --username alice --password secret
submit-job --url https://linkedin.com/jobs/123 --title "SWE" --company Acme --source LINKEDIN
postings -s LINKEDIN -j '.[].title'
analyze -i <posting-id> -j '.fitScore'
```

---

## Browser Extension

A Chrome extension for capturing job postings from LinkedIn and Indeed:

| Feature          | Description                                                |
|------------------|------------------------------------------------------------|
| **LinkedIn**     | Extracts title, company, and description from job pages    |
| **Indeed**       | Extracts title, company, and description from job pages    |
| **Login**        | Authenticates via the extension Options page               |
| **Review before submit** | Editable form before sending to the API            |

```bash
# Load the extension
chrome://extensions → Developer mode → Load unpacked → select browser-extension/
```

Then configure the server URL (`http://localhost:8880/api/graphql`) and log in via the Options
page.

---

## Testing

| Type              | Tool                            | Command                                                                        |
|-------------------|---------------------------------|--------------------------------------------------------------------------------|
| Unit              | JUnit 5 + Mockito               | `mvn clean test`                                                               |
| Mutation          | PIT                             | `mvn -Ppitest test`                                                            |
| Architecture      | ArchUnit                        | `mvn clean test -pl coverage-jacoco -am` (13 rules)                            |
| Integration       | Spring Boot Test + RestTemplate | `mvn clean verify` → `coverage-jacoco/target/site/jacoco-aggregate/index.html` |
| AOT compatibility | Spring AOT                      | `mvn -Pnative test -pl bootstrap-server -am`                                   |
| Checkstyle        | Checkstyle                      | `mvn validate` (Google Java Style, 140 cols, 2-space indent)                   |

---

## Configuration

| Key                         | Default                     | Description                                              |
|-----------------------------|-----------------------------|----------------------------------------------------------|
| `jwt.secret`                | `change-me-...`             | JWT signing key (min 32 chars, set via `JWT_SECRET` env) |
| `spring.ai.openai.api-key`  | `sk-placeholder`            | DeepSeek API key (set via `DEEPSEEK_API_KEY` env)        |
| `cache.max-size`            | `1000`                      | Caffeine max cache entries                               |
| `cache.default-ttl-seconds` | `300`                       | Cache TTL                                                |
| `server.url` (CLI)          | `http://localhost:8880/api` | GraphQL server URL                                       |
| `spring.profiles.active`    | —                           | `dev` / `loc` for file-based H2                          |

Profiles:

- **default**: H2 in-memory, Flyway auto-migration
- **dev/loc**: H2 file-based (`./data/jobtracker`)

---

## Documentation

```bash
mvn clean verify site site:stage-deploy
# ./target/site-staging/staging/index.html
```

---

## CI/CD

| Workflow             | Trigger              | What it does                                                             |
|----------------------|----------------------|--------------------------------------------------------------------------|
| `ci.yml`             | Push/PR to `develop` | `mvn verify` + SonarCloud + dependency review + AOT check                |
| `pages.yml`          | Push to `develop`    | `mvn site` → GitHub Pages                                                |
| `native-release.yml` | Tag `v*`             | Native compile both modules → Docker push (server) + release asset (CLI) |

---

## Caching

The `infrastructure-cache` module decorates persistence adapters with Caffeine caching:

- **JobApplication**: individual (`jobapp:<id>`) and per-user list (`jobapps:user:<id>`) — evicted
  on create/update/delete
- **JobPosting**: individual (`jobpost:<id>`) and per-user list (`jobposts:user:<id>`) — evicted on
  create
