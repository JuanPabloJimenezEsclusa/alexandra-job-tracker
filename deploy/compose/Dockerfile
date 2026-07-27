# This Dockerfile uses a multi-stage build to create a lightweight image for the bootstrap server.
FROM maven:3-eclipse-temurin-26 AS build

WORKDIR /app

COPY pom.xml ./
COPY domain/pom.xml domain/
COPY application/pom.xml application/
COPY adapter-persistence/pom.xml adapter-persistence/
COPY adapter-cache/pom.xml adapter-cache/
COPY adapter-auth/pom.xml adapter-auth/
COPY adapter-ai/pom.xml adapter-ai/
COPY adapter-api/pom.xml adapter-api/
COPY adapter-cli/pom.xml adapter-cli/
COPY bootstrap-server/pom.xml bootstrap-server/
COPY bootstrap-cli/pom.xml bootstrap-cli/
COPY coverage-jacoco/pom.xml coverage-jacoco/
COPY testing-pentest/pom.xml testing-pentest/

RUN --mount=type=cache,id=mvn-repo,target=/app/.m2-repo \
    mvn dependency:go-offline -Dmaven.repo.local=/app/.m2-repo -B -ntp -e -q

COPY . .

RUN --mount=type=cache,id=mvn-repo,target=/app/.m2-repo \
    mvn package -Dmaven.repo.local=/app/.m2-repo -pl bootstrap-server -am -DskipTests -B -ntp -e -q

# Download OpenTelemetry Java Agent for auto-instrumentation
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.28.1/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar

# Use a lightweight JRE image for the runtime
FROM eclipse-temurin:26-jre

# Install gosu for running the application as a non-root user and create a non-root user for better security
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/* \
    && groupadd -r appuser && useradd -r -g appuser -d /app appuser \
    && curl -fsSL "https://github.com/tianon/gosu/releases/download/1.17/gosu-amd64" -o /usr/local/bin/gosu \
    && chmod +x /usr/local/bin/gosu

WORKDIR /app

COPY --from=build /app/bootstrap-server/target/*.jar app.jar
COPY --from=build /app/opentelemetry-javaagent.jar opentelemetry-javaagent.jar

RUN chown -R appuser:appuser /app

# Create an entrypoint script to set permissions and run the application as the non-root user
COPY <<'EOF' /usr/local/bin/entrypoint.sh
#!/bin/sh
chown -R appuser:appuser /app/deploy/data 2>/dev/null || true
exec gosu appuser "$@"
EOF

RUN chmod 755 /usr/local/bin/entrypoint.sh

EXPOSE 8880

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
CMD ["java", "-javaagent:/app/opentelemetry-javaagent.jar", "-jar", "app.jar"]
