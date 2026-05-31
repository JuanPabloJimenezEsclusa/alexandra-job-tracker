# AGENTS

## Project

Multi-user CLI job tracker with GraphQL API. Hexagonal (ports & adapters) architecture. Java 25,
Spring Boot 4, Maven multi-module.

## Architecture

Three runnable artifacts from the same build:

- `bootstrap-server` — GraphQL API (`/api/graphql`), Spring AI, OpenTelemetry
- `bootstrap-cli` — Spring Shell → HTTP POST to server's GraphQL endpoint
- `browser-extension` — Chrome extension for LinkedIn/Indeed DOM capture

12 Maven modules + 1 browser extension:

| Module                    | Role |
|---------------------------|------|
| `domain`                  | Pure Java, no framework deps |
| `application`             | Use case implementations |
| `adapter-api`             | GraphQL resolvers, DTOs |
| `adapter-cli`             | Spring Shell commands, HTTP client |
| `infrastructure-persistence` | JPA, Flyway migrations |
| `infrastructure-auth`     | JWT provider, GraphQL auth interceptor |
| `infrastructure-ai`       | DeepSeek LLM integration (Spring AI) |
| `infrastructure-cache`    | Caffeine cache decorators |
| `infrastructure-observability` | OpenTelemetry, Prometheus, Micrometer |
| `bootstrap-server`        | Composition root — server |
| `bootstrap-cli`           | Composition root — CLI |
| `coverage-jacoco`         | JaCoCo aggregation + ArchUnit tests |

All resolvers follow **CQRS** — `*QueryResolver` for reads, `*MutationResolver` for writes,
never mixed.

## Build & Run

```bash
mvn validate -N                    # root POM validation (checkstyle)
mvn compile -pl <module> -am       # compile module + dependencies
mvn test -pl <module>              # single module tests
mvn test -pl coverage-jacoco -am   # architecture tests (13 ArchUnit rules)
mvn verify                         # full CI (unit + integration + arch)
mvn package -pl bootstrap-server -am -DskipTests  # server JAR
mvn package -pl bootstrap-cli -am -DskipTests     # CLI JAR
mvn install -DskipTests && mvn -Pnative native:compile -pl bootstrap-server -DskipTests  # native binary
docker compose -f deploy/compose/docker-compose.yml up -d  # all infra
java -jar bootstrap-cli/target/bootstrap-cli-*.jar --server.url=http://localhost:8880  # CLI
```

## Testing

- ArchUnit: `mvn test -pl coverage-jacoco -am` (13 rules across all modules)
  - 4 critical boundary rules (domain → no Spring, domain → no infra, application → no adapters, domain → no outer layers)
  - 9 per-module `resideInAnyPackage` whitelists
  - Migrated from `domain` → `bootstrap-server` → `coverage-jacoco` module
- Checkstyle: `mvn validate` (Google Java Style with overrides, config at
  `src/main/resources/checkstyle.xml`)
- AOT compatibility: `mvn -Pnative test-compile -pl bootstrap-server -am` (validates Spring AOT
  processing)

## Key Config

- `application.yml` per profile (`server` / `cli`) in `bootstrap-*/src/main/resources/`
- JWT secret: env var `JWT_SECRET` (min 32 chars)
- DeepSeek: env var `DEEPSEEK_API_KEY`
- Flyway migrations: `infrastructure-persistence/src/main/resources/db/migration/V*.sql`
  - `V1__init.sql` — schema
  - `V2__seed_user.sql` — seed data
  - `V3__widen_job_postings_columns.sql` — widen title/company to TEXT
- `.editorconfig`: 2-space indent, LF, max 140 chars
- `.mvn/jvm.config`: `-Xmx2g`

## Browser Extension

Located in `browser-extension/` (not a Maven module — plain JS/HTML/CSS).

Load via `chrome://extensions` → Developer mode → Load unpacked.

Uses content scripts for LinkedIn (`/jobs/view/*`) and Indeed (`/viewjob/*`). Extracts
title/company/description from the DOM with polling and async rendering support.

## Site & Reporting

```bash
mvn site site:stage              # aggregated site with javadocs + checkstyle
mvn javadoc:aggregate            # javadocs only
mvn checkstyle:checkstyle        # checkstyle report only
```

Output: `target/site-staging/` (served to GitHub Pages on push to `main`).

## CI/CD

3 GitHub Actions workflows in `.github/workflows/`:

- `ci.yml` — `mvn verify` + SonarCloud + dependency review + native AOT check
- `pages.yml` — `mvn site site:stage-deploy` → GitHub Pages
- `native-release.yml` — `native:compile` → Docker push to GHCR (tag `v*`)

Dependabot configured for Maven (grouped: Spring, testing, otel), Docker, GitHub Actions.
