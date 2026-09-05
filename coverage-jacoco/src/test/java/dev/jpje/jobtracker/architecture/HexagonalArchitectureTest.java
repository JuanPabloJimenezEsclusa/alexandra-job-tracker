package dev.jpje.jobtracker.architecture;

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
@AnalyzeClasses(packages = "dev.jpje.jobtracker")
class HexagonalArchitectureTest {

  private static final String DOMAIN = "dev.jpje.jobtracker.domain..";
  private static final String APPLICATION = "dev.jpje.jobtracker.application..";
  private static final String ADAPTER_API = "dev.jpje.jobtracker.api..";
  private static final String ADAPTER_PERSISTENCE = "dev.jpje.jobtracker.persistence..";
  private static final String ADAPTER_AUTH = "dev.jpje.jobtracker.auth..";
  private static final String ADAPTER_AI = "dev.jpje.jobtracker.ai..";
  private static final String ADAPTER_CACHE = "dev.jpje.jobtracker.cache..";
  private static final String ADAPTER_CLI = "dev.jpje.jobtracker.cli..";

  // --- Common allowed packages (applied to all layers) ---

  private static final String[] COMMON = {
    "java..",
    "org.jspecify.."
  };

  // --- Critical hexagonal boundary rules ---

  @ArchTest
  static final ArchRule DOMAIN_MUST_NOT_DEPEND_ON_SPRING = noClasses()
    .that().resideInAPackage(DOMAIN)
    .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
    .as("Domain must not depend on Spring")
    .because("the domain layer is pure Java with zero framework imports");

  @ArchTest
  static final ArchRule APPLICATION_MUST_NOT_DEPEND_ON_ADAPTERS = noClasses()
    .that().resideInAPackage(APPLICATION)
    .should().dependOnClassesThat().resideInAnyPackage(
      "..ai..", "..api..", "..auth..", "..cache..", "..cli..", "..persistence..")
    .allowEmptyShould(true)
    .as("Application must not depend on adapters, CLI, or API")
    .because("application implements use cases independently of delivery mechanisms");

  @ArchTest
  static final ArchRule DOMAIN_MUST_NOT_DEPEND_ON_OUTER_LAYERS = noClasses()
    .that().resideInAPackage(DOMAIN)
    .should().dependOnClassesThat().resideInAnyPackage(
      "..usecase..", "..ai..", "..api..", "..auth..", "..cache..", "..cli..", "..persistence..")
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
      concat(DOMAIN, APPLICATION))
    .as("Application module dependencies")
    .because("application depends on domain types and standard libraries");

  @ArchTest
  static final ArchRule ADAPTER_API_DEPENDENCIES = classes()
    .that().resideInAPackage(ADAPTER_API)
    .should().onlyDependOnClassesThat().resideInAnyPackage(
      concat(DOMAIN, ADAPTER_API,
        "org.springframework.(stereotype|context|graphql|http|web)..",
        "graphql..",
        "org.slf4j..",
        "reactor.core.."))
    .as("Adapter API module dependencies")
    .because("the GraphQL adapter translates HTTP to use case calls");

  @ArchTest
  static final ArchRule ADAPTER_PERSISTENCE_DEPENDENCIES = classes()
    .that().resideInAPackage(ADAPTER_PERSISTENCE)
    .should().onlyDependOnClassesThat().resideInAnyPackage(
      concat(DOMAIN, ADAPTER_PERSISTENCE,
        "jakarta.persistence..",
        "org.hibernate..",
        "org.springframework.(stereotype|data|dao|transaction)..",
        "org.flywaydb.."))
    .as("Persistence module dependencies")
    .because("persistence implements domain ports with JPA and Flyway");

  @ArchTest
  static final ArchRule ADAPTER_AUTH_DEPENDENCIES = classes()
    .that().resideInAPackage(ADAPTER_AUTH)
    .should().onlyDependOnClassesThat().resideInAnyPackage(
      concat(DOMAIN, ADAPTER_AUTH,
        "io.jsonwebtoken..",
        "jakarta.servlet..",
        "javax.crypto..",
        "org.mindrot.jbcrypt..",
        "org.springframework.(aot|stereotype|context|beans|boot).."))
    .as("Auth module dependencies")
    .because("auth implements JWT token generation and GraphQL auth interceptor");

  @ArchTest
  static final ArchRule ADAPTER_AI_DEPENDENCIES = classes()
    .that().resideInAPackage(ADAPTER_AI)
    .should().onlyDependOnClassesThat().resideInAnyPackage(
      concat(DOMAIN, ADAPTER_AI,
        "org.springframework.(aot|stereotype|context|beans|boot|core|ai)..",
        "org.springaicommunity.agent.(utils|tools)..",
        "com.fasterxml.jackson.."))
    .as("AI module dependencies")
    .because("the AI adapter integrates with LLMs via Spring AI");

  @ArchTest
  static final ArchRule ADAPTER_CACHE_DEPENDENCIES = classes()
    .that().resideInAPackage(ADAPTER_CACHE)
    .should().onlyDependOnClassesThat().resideInAnyPackage(
      concat(DOMAIN, ADAPTER_CACHE,
        "com.github.benmanes.caffeine..",
        "org.springframework.(stereotype|context|beans|cache|boot).."))
    .as("Cache module dependencies")
    .because("cache decorates persistence adapters with Caffeine");

  @ArchTest
  static final ArchRule ADAPTER_CLI_DEPENDENCIES = classes()
    .that().resideInAPackage(ADAPTER_CLI)
    .should().onlyDependOnClassesThat().resideInAnyPackage(
      concat(ADAPTER_CLI,
        "org.springframework.(stereotype|context|beans|core|boot|lang|shell)..",
        "com.fasterxml.jackson..",
        "net.thisptr.jackson.jq.."))
    .as("Adapter CLI module dependencies")
    .because("the CLI adapter is a standalone Spring Shell client");

  // --- Hexagonal best practices ---

  @ArchTest
  static final ArchRule API_ADAPTER_ISOLATED = adapterIsolationRule(
    "API adapter", ADAPTER_API, ADAPTER_PERSISTENCE, ADAPTER_AUTH, ADAPTER_AI, ADAPTER_CACHE, ADAPTER_CLI);

  @ArchTest
  static final ArchRule PERSISTENCE_ADAPTER_ISOLATED = adapterIsolationRule(
    "Persistence adapter", ADAPTER_PERSISTENCE, ADAPTER_API, ADAPTER_AUTH, ADAPTER_AI, ADAPTER_CACHE, ADAPTER_CLI);

  @ArchTest
  static final ArchRule AUTH_ADAPTER_ISOLATED = adapterIsolationRule(
    "Auth adapter", ADAPTER_AUTH, ADAPTER_API, ADAPTER_PERSISTENCE, ADAPTER_AI, ADAPTER_CACHE, ADAPTER_CLI);

  @ArchTest
  static final ArchRule AI_ADAPTER_ISOLATED = adapterIsolationRule(
    "AI adapter", ADAPTER_AI, ADAPTER_API, ADAPTER_PERSISTENCE, ADAPTER_AUTH, ADAPTER_CACHE, ADAPTER_CLI);

  @ArchTest
  static final ArchRule CACHE_ADAPTER_ISOLATED = adapterIsolationRule(
    "Cache adapter", ADAPTER_CACHE, ADAPTER_API, ADAPTER_PERSISTENCE, ADAPTER_AUTH, ADAPTER_AI, ADAPTER_CLI);

  @ArchTest
  static final ArchRule CLI_ADAPTER_ISOLATED = adapterIsolationRule(
    "CLI adapter", ADAPTER_CLI, ADAPTER_API, ADAPTER_PERSISTENCE, ADAPTER_AUTH, ADAPTER_AI, ADAPTER_CACHE);

  @ArchTest
  static final ArchRule PERSISTENCE_INTERNALS_CONFINED = noClasses()
    .that().resideOutsideOfPackage(ADAPTER_PERSISTENCE)
    .should().dependOnClassesThat().resideInAnyPackage(
      "dev.jpje.jobtracker.persistence.entity..",
      "dev.jpje.jobtracker.persistence.repository..")
    .as("Persistence internals stay in the persistence adapter")
    .because("JPA entities and repositories are adapter types that must not leak into the core (boundary isolation)");

  private static ArchRule adapterIsolationRule(final String label, final String adapter,
                                               final String... siblings) {
    return noClasses()
      .that().resideInAPackage(adapter)
      .should().dependOnClassesThat().resideInAnyPackage(siblings)
      .as(label + " must not depend on other adapters")
      .because("adapters reach the core only through ports; cross-adapter wiring happens in the composition root");
  }

  // --- Helper ---

  private static String[] concat(String... rest) {
    var result = new String[HexagonalArchitectureTest.COMMON.length + rest.length];
    System.arraycopy(HexagonalArchitectureTest.COMMON, 0, result, 0, HexagonalArchitectureTest.COMMON.length);
    System.arraycopy(rest, 0, result, HexagonalArchitectureTest.COMMON.length, rest.length);
    return result;
  }
}
