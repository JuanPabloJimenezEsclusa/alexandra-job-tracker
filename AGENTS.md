# AGENTS

## Project

Multi-user CLI job tracker with GraphQL API. Hexagonal (ports & adapters) architecture. Java 25,
Spring Boot 4, Maven multi-module.

## Architecture

Two JARs from the same build:

- `bootstrap-server` — GraphQL API (`/graphql`), PostgreSQL, Spring AI, OpenTelemetry
- `bootstrap-cli` — Spring Shell → HTTP POST to server's GraphQL endpoint

11 modules: `domain` (pure Java, no framework deps), `application`, `infrastructure-*`, `adapter-*`,
`bootstrap-*`.

Domain layer validated by ArchUnit — must not depend on Spring or any infrastructure module.

## Build & Run

```bash
mvn validate -N                    # root POM validation
mvn compile -pl <module> -am       # compile module + dependencies
mvn test -pl <module>              # single module tests
mvn verify                         # full CI (needs Postgres running)
mvn package -pl bootstrap-server -am -DskipTests  # server JAR
mvn install -DskipTests && mvn -Pnative native:compile -pl bootstrap-server -DskipTests  # native binary
docker compose -f deploy/compose/docker-compose.yml up -d  # all infra
java -jar bootstrap-cli/target/bootstrap-cli-*.jar --server.url=http://localhost:8080  # CLI
```

## Testing

- ArchUnit: `mvn test -pl domain` (3 hexagonal boundary rules)
- Checkstyle: `mvn validate` (Google Java Style with overrides, config at
  `src/main/resources/checkstyle.xml`)
- E2E: tagged `@Tag("E2ETest")`, run via `mvn verify` (requires Postgres or Testcontainers)
- JaCoCo aggregated report: `**/target/site/jacoco-aggregate/index.html`
- AOT compatibility: `mvn -Pnative test-compile -pl bootstrap-server -am` (validates Spring AOT
  processing)

## Key Config

- `application.yml` per profile (`server` / `cli`) in `bootstrap-*/src/main/resources/`
- JWT secret: env var `JWT_SECRET` (min 32 chars)
- DeepSeek: env var `DEEPSEEK_API_KEY`
- Flyway migration: `infrastructure-persistence/src/main/resources/db/migration/V1__init.sql`
- `.editorconfig`: 2-space indent, LF, max 100 chars
- `.mvn/jvm.config`: `-Xmx2g`

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
