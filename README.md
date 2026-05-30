[![License](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

<p align="center">
  <a href="https://alistair.cockburn.us/hexagonal-architecture/"><img src="https://img.shields.io/badge/Architecture-Hexagonal-brightgreen?style=for-the-badge" alt="Hexagonal Architecture"/></a>
  <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/Spring%20Boot-4.0-brightgreen?style=for-the-badge" alt="Spring Boot 4.0"/></a>
  <a href="https://www.graalvm.org/"><img src="https://img.shields.io/badge/GraalVM-Native-005571?style=for-the-badge" alt="GraalVM Native"/></a>
  <a href="https://graphql.org/"><img src="https://img.shields.io/badge/API-GraphQL-E10098?style=for-the-badge" alt="GraphQL"/></a>
  <a href="https://spring.io/projects/spring-shell"><img src="https://img.shields.io/badge/CLI-Spring%20Shell-6DB33F?style=for-the-badge" alt="Spring Shell"/></a>
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

### Layer Constraints

```
domain  →  application  →  infrastructure-*  +  adapter-*
                                          ↘              ↙
                                     bootstrap-server / bootstrap-cli
```

- `domain` is pure Java — zero framework imports. Validated by ArchUnit.
- `adapter-*` and `infrastructure-*` depend on `domain` and `application`.
- `bootstrap-*` is the composition root — depends on everything, wires adapters.

### Data Flow

```
CLI (Spring Shell)  ──HTTP──►  GraphQL API  ──►  Resolver  ──►  Use Case  ──►  Port
                                                                                   │
                                             H2  ◄──  JPA Adapter  ◄───────────────┘
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
mvn test -pl domain                               # Domain & ArchUnit tests
mvn verify                                        # Full CI (unit + E2E)
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
| `register(username, password)`                  | Create account, returns JWT                      |
| `login(username, password)`                     | Authenticate, returns JWT                        |
| `me`                                            | Current user info                                |
| `applications(status, source)`                  | List job applications with optional filters      |
| `createApplication(company, role, source, ...)` | Track a new application                          |
| `updateApplicationStatus(id, status)`           | Move through pipeline                            |
| `deleteApplication(id)`                         | Remove an application                            |
| `analytics(since)`                              | Aggregated per-status counts and conversion rate |
| `jobPostings(source)`                           | List scraped job postings                        |
| `scrapeJobPosting(url)`                         | Scrape a job posting from LinkedIn               |
| `analyzeJobPosting(id)`                         | AI-powered job description analysis              |

### Status Pipeline

```
SAVED → APPLIED → INTERVIEWING → OFFER → ACCEPTED
                             ↘          ↘
                            REJECTED   WITHDRAWN
```

---

## Testing

| Type              | Tool                            | Command                                                                        |
|-------------------|---------------------------------|--------------------------------------------------------------------------------|
| Unit              | JUnit 5 + Mockito               | `mvn clean test`                                                               |
| Mutation testing  | PIT                             | `mvn -Ppitest test`                                                            |
| Architecture      | ArchUnit                        | `mvn clean test -pl domain`                                                    |
| E2E               | Spring Boot Test + RestTemplate | `mvn clean verify` → `coverage-jacoco/target/site/jacoco-aggregate/index.html` |
| AOT compatibility | Spring AOT                      | `mvn -Pnative test -pl bootstrap-server -am`                                   |

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
