package com.jobtracker.api.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Hexagonal architecture tests.
 *
 * <p>Scans all non-bootstrap modules. Each module's allowed dependencies are explicitly
 * whitelisted. Bootstrap modules are not on the classpath (excluded from coverage-jacoco
 * dependencies), so they are not scanned.
 */
@AnalyzeClasses(packages = "com.jobtracker")
class HexagonalArchitectureTest {

  private static final String DOMAIN = "com.jobtracker.domain..";
  private static final String APPLICATION = "com.jobtracker.application..";
  private static final String ADAPTER_API = "com.jobtracker.api..";
  private static final String ADAPTER_CLI = "com.jobtracker.cli..";
  private static final String INFRA_PERSISTENCE = "com.jobtracker.persistence..";
  private static final String INFRA_AUTH = "com.jobtracker.auth..";
  private static final String INFRA_AI = "com.jobtracker.ai..";
  private static final String INFRA_CACHE = "com.jobtracker.cache..";
  private static final String INFRA_OBSERVABILITY = "com.jobtracker.observability..";

  // --- Common allowed packages (applied to all layers) ---

  private static final String[] COMMON = {
    "java..",
    "org.jspecify..",
    "org.slf4j..",
    "com.tngtech.archunit..",
    "org.junit..",
    "org.assertj.core.api..",
    "org.mockito..",
    "org.springframework.test.."
  };

  // --- Critical hexagonal boundary rules ---

  @ArchTest
  static final ArchRule DOMAIN_MUST_NOT_DEPEND_ON_SPRING = noClasses()
    .that().resideInAPackage(DOMAIN)
    .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
    .as("Domain must not depend on Spring")
    .because("the domain layer is pure Java with zero framework imports");

  @ArchTest
  static final ArchRule DOMAIN_MUST_NOT_DEPEND_ON_INFRASTRUCTURE = noClasses()
    .that().resideInAPackage(DOMAIN)
    .should().dependOnClassesThat().resideInAnyPackage(
      "..persistence..", "..auth..", "..ai..", "..cache..", "..observability..")
    .as("Domain must not depend on infrastructure")
    .because("infrastructure details must not leak into the domain");

  @ArchTest
  static final ArchRule APPLICATION_MUST_NOT_DEPEND_ON_ADAPTERS = noClasses()
    .that().resideInAPackage(APPLICATION)
    .should().dependOnClassesThat().resideInAnyPackage("..adapter..", "..cli..", "..api..")
    .allowEmptyShould(true)
    .as("Application must not depend on adapters, CLI, or API")
    .because("application implements use cases independently of delivery mechanisms");

  @ArchTest
  static final ArchRule DOMAIN_MUST_NOT_DEPEND_ON_OUTER_LAYERS = noClasses()
    .that().resideInAPackage(DOMAIN)
    .should().dependOnClassesThat().resideInAnyPackage(
      "..application..", "..api..", "..cli..",
      "..persistence..", "..auth..", "..ai..", "..cache..", "..observability..")
    .allowEmptyShould(true)
    .as("Domain must not depend on application, adapters, or infrastructure")
    .because("domain is the innermost layer with no outgoing dependencies to other layers");

  // --- Per-module dependency whitelists ---

  @ArchTest
  static final ArchRule DOMAIN_DEPENDENCIES = classes()
    .that().resideInAPackage(DOMAIN)
    .should().onlyDependOnClassesThat().resideInAnyPackage(
      concat(DOMAIN))
    .as("Domain module dependencies")
    .because("domain is pure Java and must only use standard libraries");

  @ArchTest
  static final ArchRule APPLICATION_DEPENDENCIES = classes()
    .that().resideInAPackage(APPLICATION)
    .should().onlyDependOnClassesThat().resideInAnyPackage(
      concat(DOMAIN, APPLICATION, "org.mindrot.jbcrypt.."))
    .as("Application module dependencies")
    .because("application depends on domain types and standard libraries");

  @ArchTest
  static final ArchRule ADAPTER_API_DEPENDENCIES = classes()
    .that().resideInAPackage(ADAPTER_API)
    .should().onlyDependOnClassesThat().resideInAnyPackage(
      concat(DOMAIN, APPLICATION, ADAPTER_API,
        "org.springframework..",
        "org.springframework.boot..",
        "graphql..",
        "reactor.core.."))
    .as("Adapter API module dependencies")
    .because("the GraphQL adapter translates HTTP to use case calls");

  @ArchTest
  static final ArchRule ADAPTER_CLI_DEPENDENCIES = classes()
    .that().resideInAPackage(ADAPTER_CLI)
    .should().onlyDependOnClassesThat().resideInAnyPackage(
      concat(ADAPTER_CLI,
        "org.springframework.shell..",
        "org.springframework.(stereotype|context|beans|core|boot|lang)..",
        "com.fasterxml.jackson..",
        "net.thisptr.jackson.jq.."))
    .as("Adapter CLI module dependencies")
    .because("the CLI adapter is a standalone Spring Shell client");

  @ArchTest
  static final ArchRule INFRA_PERSISTENCE_DEPENDENCIES = classes()
    .that().resideInAPackage(INFRA_PERSISTENCE)
    .should().onlyDependOnClassesThat().resideInAnyPackage(
      concat(DOMAIN, INFRA_PERSISTENCE,
        "jakarta.persistence..",
        "org.hibernate..",
        "org.springframework..",
        "org.springframework.boot..",
        "org.flywaydb.."))
    .as("Persistence module dependencies")
    .because("persistence implements domain ports with JPA and Flyway");

  @ArchTest
  static final ArchRule INFRA_AUTH_DEPENDENCIES = classes()
    .that().resideInAPackage(INFRA_AUTH)
    .should().onlyDependOnClassesThat().resideInAnyPackage(
      concat(DOMAIN, INFRA_AUTH,
        "io.jsonwebtoken..",
        "jakarta.servlet..",
        "javax.crypto..",
        "graphql..",
        "reactor.core..",
        "org.springframework.."))
    .as("Auth module dependencies")
    .because("auth implements JWT token generation and GraphQL auth interceptor");

  @ArchTest
  static final ArchRule INFRA_AI_DEPENDENCIES = classes()
    .that().resideInAPackage(INFRA_AI)
    .should().onlyDependOnClassesThat().resideInAnyPackage(
      concat(DOMAIN, INFRA_AI,
        "org.springframework.(stereotype|context|beans|boot|core|ai)..",
        "com.fasterxml.jackson.."))
    .as("AI module dependencies")
    .because("the AI adapter integrates with DeepSeek via Spring AI");

  @ArchTest
  static final ArchRule INFRA_CACHE_DEPENDENCIES = classes()
    .that().resideInAPackage(INFRA_CACHE)
    .should().onlyDependOnClassesThat().resideInAnyPackage(
      concat(DOMAIN, INFRA_CACHE,
        "com.github.benmanes.caffeine..",
        "org.springframework.(stereotype|context|beans|cache|boot).."))
    .as("Cache module dependencies")
    .because("cache decorates persistence adapters with Caffeine");

  @ArchTest
  static final ArchRule INFRA_OBSERVABILITY_DEPENDENCIES = classes()
    .that().resideInAPackage(INFRA_OBSERVABILITY)
    .should().onlyDependOnClassesThat().resideInAnyPackage(
      concat(INFRA_OBSERVABILITY,
        "io.micrometer..",
        "io.opentelemetry..",
        "org.springframework..",
        "org.springframework.boot.."))
    .as("Observability module dependencies")
    .because("observability configures Micrometer and OpenTelemetry");

  // --- Helper ---

  private static String[] concat(String... rest) {
    var result = new String[HexagonalArchitectureTest.COMMON.length + rest.length];
    System.arraycopy(HexagonalArchitectureTest.COMMON, 0, result, 0, HexagonalArchitectureTest.COMMON.length);
    System.arraycopy(rest, 0, result, HexagonalArchitectureTest.COMMON.length, rest.length);
    return result;
  }
}
