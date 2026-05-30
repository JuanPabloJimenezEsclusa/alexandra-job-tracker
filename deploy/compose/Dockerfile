# This Dockerfile uses a multi-stage build to create a lightweight image for the bootstrap server.
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

COPY pom.xml ./
COPY domain/pom.xml domain/
COPY application/pom.xml application/
COPY infrastructure-persistence/pom.xml infrastructure-persistence/
COPY infrastructure-cache/pom.xml infrastructure-cache/
COPY infrastructure-auth/pom.xml infrastructure-auth/
COPY infrastructure-ai/pom.xml infrastructure-ai/
COPY infrastructure-observability/pom.xml infrastructure-observability/
COPY adapter-api/pom.xml adapter-api/
COPY adapter-cli/pom.xml adapter-cli/
COPY bootstrap-server/pom.xml bootstrap-server/
COPY bootstrap-cli/pom.xml bootstrap-cli/

RUN --mount=type=cache,id=mvn-repo,target=/app/.m2-repo \
    mvn dependency:go-offline -Dmaven.repo.local=/app/.m2-repo -B -q

COPY . .

RUN --mount=type=cache,id=mvn-repo,target=/app/.m2-repo \
    mvn package -Dmaven.repo.local=/app/.m2-repo -pl bootstrap-server -am -DskipTests -B -q

# Use a lightweight JRE image for the runtime
FROM eclipse-temurin:25-jre

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /app/bootstrap-server/target/*.jar app.jar

EXPOSE 8880

ENTRYPOINT ["java", "-jar", "app.jar"]
