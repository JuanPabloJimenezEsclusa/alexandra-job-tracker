package dev.jpje.jobtracker.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.context.annotation.Configuration;

@AnalyzeClasses(packages = "dev.jpje.jobtracker")
class HexagonalArchitectureTest {

  private static final String DOMAIN = "dev.jpje.jobtracker.domain..";
  private static final String APPLICATION = "dev.jpje.jobtracker.application..";
  private static final String ADAPTER_API = "dev.jpje.jobtracker.api..";
  private static final String ADAPTER_PERSISTENCE = "dev.jpje.jobtracker.persistence..";
  private static final String ADAPTER_AUTH = "dev.jpje.jobtracker.auth..";
  private static final String ADAPTER_AI = "dev.jpje.jobtracker.ai..";
  private static final String ADAPTER_CACHE = "dev.jpje.jobtracker.cache..";
  private static final String ADAPTER_EVENTS = "dev.jpje.jobtracker.events..";
  private static final String BOOTSTRAP_SERVER = "dev.jpje.jobtracker.server..";
  private static final String CLI_CLIENT = "dev.jpje.jobtracker.cli..";
  private static final String BOOTSTRAP_CLI = "dev.jpje.jobtracker.bootstrap..";

  private static final String[] COMMON = {
    "java..",
    "org.jspecify.."
  };

  private static final DescribedPredicate<JavaClass> APPLICATION_INTERNALS = new DescribedPredicate<>(
    "reside in the application module outside application.port") {
    @Override
    public boolean test(final JavaClass javaClass) {
      final var name = javaClass.getName();
      return name.startsWith("dev.jpje.jobtracker.application.")
        && !name.startsWith("dev.jpje.jobtracker.application.port.");
    }
  };

  // --- Critical hexagonal boundary rules ---

  @ArchTest
  static final ArchRule DOMAIN_MUST_NOT_DEPEND_ON_SPRING = noClasses()
    .that().resideInAPackage(DOMAIN)
    .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
    .as("Domain must not depend on Spring")
    .because("domain layer is pure Java with zero framework imports");

  @ArchTest
  static final ArchRule APPLICATION_MUST_NOT_DEPEND_ON_SPRING = noClasses()
    .that().resideInAPackage(APPLICATION)
    .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
    .as("Application must not depend on Spring")
    .because("transaction boundaries live in the composition root, never in the framework-free application layer");

  @ArchTest
  static final ArchRule APPLICATION_MUST_NOT_DEPEND_ON_ADAPTERS = noClasses()
    .that().resideInAPackage(APPLICATION)
    .should().dependOnClassesThat().resideInAnyPackage(
      "..ai..", "..api..", "..auth..", "..cache..", "..persistence..", "..cli..")
    .allowEmptyShould(true)
    .as("Application must not depend on adapters, CLI, or API")
    .because("application implements use cases independently of delivery mechanisms");

  @ArchTest
  static final ArchRule DOMAIN_MUST_NOT_DEPEND_ON_OUTER_LAYERS = noClasses()
    .that().resideInAPackage(DOMAIN)
    .should().dependOnClassesThat().resideInAnyPackage(
      "..usecase..", "..ai..", "..api..", "..auth..", "..cache..", "..persistence..", "..cli..")
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
        "jakarta.servlet..",
        "org.springframework.(stereotype|context|beans|graphql|http|web|security)..",
        "graphql..",
        "org.slf4j..",
        "reactor.core.."))
    .as("Adapter API module dependencies")
    .because("GraphQL adapter translates HTTP to use case calls and enforces HTTP security");

  @ArchTest
  static final ArchRule ADAPTER_PERSISTENCE_DEPENDENCIES = classes()
    .that().resideInAPackage(ADAPTER_PERSISTENCE)
    .should().onlyDependOnClassesThat().resideInAnyPackage(
      concat(DOMAIN, ADAPTER_PERSISTENCE,
        "jakarta.persistence..",
        "org.hibernate..",
        "org.springframework.(aot|stereotype|data|dao|transaction|beans|boot)..",
        "org.flywaydb.."))
    .as("Persistence module dependencies")
    .because("persistence implements domain ports with JPA and Flyway");

  @ArchTest
  static final ArchRule ADAPTER_AUTH_DEPENDENCIES = classes()
    .that().resideInAPackage(ADAPTER_AUTH)
    .should().onlyDependOnClassesThat().resideInAnyPackage(
      concat(DOMAIN, APPLICATION, ADAPTER_AUTH,
        "com.nimbusds..",
        "javax.crypto..",
        "org.springframework.(aot|stereotype|context|beans|boot|security).."))
    .as("Auth module dependencies")
    .because("auth implements JWT token generation, password hashing, and user authentication");

  @ArchTest
  static final ArchRule ADAPTER_AI_DEPENDENCIES = classes()
    .that().resideInAPackage(ADAPTER_AI)
    .should().onlyDependOnClassesThat().resideInAnyPackage(
      concat(DOMAIN, ADAPTER_AI,
        "org.springframework.(aot|stereotype|context|beans|boot|core|ai)..",
        "org.springaicommunity.agent.(utils|tools)..",
        "com.fasterxml.jackson.."))
    .as("AI module dependencies")
    .because("AI adapter integrates with LLMs via Spring AI");

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
  static final ArchRule ADAPTER_EVENTS_DEPENDENCIES = classes()
    .that().resideInAPackage(ADAPTER_EVENTS)
    .should().onlyDependOnClassesThat().resideInAnyPackage(
      concat(DOMAIN, APPLICATION, ADAPTER_EVENTS,
        "io.awspring.cloud.sns..",
        "software.amazon.awssdk.services..",
        "org.springframework.(aot|stereotype|context|http|web|transaction|scheduling|beans|boot)..",
        "org.slf4j..",
        "tools.jackson..",
        "com.fasterxml.jackson.."))
    .as("Events adapter module dependencies")
    .because("events adapter translates Spring/SQS events to ports and publishes via Spring/SNS");

  @ArchTest
  static final ArchRule CLI_CLIENT_DEPENDENCIES = classes()
    .that().resideInAPackage(CLI_CLIENT)
    .should().onlyDependOnClassesThat().resideInAnyPackage(
      concat(CLI_CLIENT,
        "org.springframework.(stereotype|context|beans|core|boot|lang|shell)..",
        "com.fasterxml.jackson..",
        "net.thisptr.jackson.jq.."))
    .as("CLI client module dependencies")
    .because("CLI adapter is a standalone Spring Shell client");

  // --- Hexagonal best practices ---

  @ArchTest
  static final ArchRule API_ADAPTER_ISOLATED = adapterIsolationRule(
    "API adapter", ADAPTER_API, ADAPTER_PERSISTENCE, ADAPTER_AUTH, ADAPTER_AI, ADAPTER_CACHE,
    ADAPTER_EVENTS, CLI_CLIENT);

  @ArchTest
  static final ArchRule PERSISTENCE_ADAPTER_ISOLATED = adapterIsolationRule(
    "Persistence adapter", ADAPTER_PERSISTENCE, ADAPTER_API, ADAPTER_AUTH, ADAPTER_AI,
    ADAPTER_CACHE, ADAPTER_EVENTS, CLI_CLIENT);

  @ArchTest
  static final ArchRule AUTH_ADAPTER_ISOLATED = adapterIsolationRule(
    "Auth adapter", ADAPTER_AUTH, ADAPTER_API, ADAPTER_PERSISTENCE, ADAPTER_AI, ADAPTER_CACHE,
    ADAPTER_EVENTS, CLI_CLIENT);

  @ArchTest
  static final ArchRule AI_ADAPTER_ISOLATED = adapterIsolationRule(
    "AI adapter", ADAPTER_AI, ADAPTER_API, ADAPTER_PERSISTENCE, ADAPTER_AUTH, ADAPTER_CACHE,
    ADAPTER_EVENTS, CLI_CLIENT);

  @ArchTest
  static final ArchRule CACHE_ADAPTER_ISOLATED = adapterIsolationRule(
    "Cache adapter", ADAPTER_CACHE, ADAPTER_API, ADAPTER_PERSISTENCE, ADAPTER_AUTH, ADAPTER_AI,
    ADAPTER_EVENTS, CLI_CLIENT);

  @ArchTest
  static final ArchRule EVENTS_ADAPTER_ISOLATED = adapterIsolationRule(
    "Events adapter", ADAPTER_EVENTS, ADAPTER_API, ADAPTER_PERSISTENCE, ADAPTER_AUTH, ADAPTER_AI,
    ADAPTER_CACHE, CLI_CLIENT);

  @ArchTest
  static final ArchRule CLI_CLIENT_ISOLATED = adapterIsolationRule(
    "CLI client", CLI_CLIENT, ADAPTER_API, ADAPTER_PERSISTENCE, ADAPTER_AUTH, ADAPTER_AI,
    ADAPTER_CACHE, ADAPTER_EVENTS);

  @ArchTest
  static final ArchRule PERSISTENCE_INTERNALS_CONFINED = noClasses()
    .that().resideOutsideOfPackage(ADAPTER_PERSISTENCE)
    .should().dependOnClassesThat().resideInAnyPackage(
      "dev.jpje.jobtracker.persistence.entity..",
      "dev.jpje.jobtracker.persistence.repository..")
    .as("Persistence internals stay in the persistence adapter")
    .because("JPA entities and repositories are adapter types that must not leak into the core (boundary isolation)");

  // --- Composition root rules ---

  @ArchTest
  static final ArchRule CORE_AND_ADAPTERS_MUST_NOT_DEPEND_ON_BOOTSTRAP = noClasses()
    .that().resideInAnyPackage(DOMAIN, APPLICATION, ADAPTER_API, ADAPTER_PERSISTENCE, ADAPTER_AUTH,
      ADAPTER_AI, ADAPTER_CACHE, ADAPTER_EVENTS, CLI_CLIENT)
    .should().dependOnClassesThat().resideInAnyPackage(BOOTSTRAP_SERVER, BOOTSTRAP_CLI)
    .as("Core and adapters must not depend on bootstrap modules")
    .because("bootstrap is the composition root and dependencies point inward");

  @ArchTest
  static final ArchRule ADAPTERS_MUST_NOT_DEPEND_ON_USE_CASE_IMPLEMENTATIONS = noClasses()
    .that().resideInAnyPackage(ADAPTER_API, ADAPTER_PERSISTENCE, ADAPTER_AUTH, ADAPTER_AI,
      ADAPTER_CACHE, ADAPTER_EVENTS, CLI_CLIENT)
    .should().dependOnClassesThat().resideInAPackage("dev.jpje.jobtracker.application.usecase..")
    .as("Adapters must reach the application through inbound ports")
    .because("use-case implementations are assembled only in the bootstrap composition root");

  @ArchTest
  static final ArchRule ADAPTERS_MUST_NOT_DEPEND_ON_APPLICATION_INTERNALS = noClasses()
    .that().resideInAnyPackage(ADAPTER_API, ADAPTER_PERSISTENCE, ADAPTER_AUTH, ADAPTER_AI,
      ADAPTER_CACHE, ADAPTER_EVENTS, CLI_CLIENT)
    .should().dependOnClassesThat(APPLICATION_INTERNALS)
    .as("Adapters must reach the application core only through application.port")
    .because("non-port application classes are internal details assembled by the composition root");

  @ArchTest
  static final ArchRule BOOTSTRAP_NON_CONFIG_MUST_NOT_DEPEND_ON_OUTBOUND_PORTS = noClasses()
    .that().resideInAnyPackage(BOOTSTRAP_SERVER, BOOTSTRAP_CLI)
    .and().areNotAnnotatedWith(Configuration.class)
    .should().dependOnClassesThat().resideInAPackage("dev.jpje.jobtracker.application.port.outbound..")
    .as("Composition root only wires outbound ports from @Configuration classes")
    .because("business orchestration belongs in the application layer, not in bootstrap");

  // --- Helper ---

  private static ArchRule adapterIsolationRule(final String label, final String adapter,
                                               final String... siblings) {
    return noClasses()
      .that().resideInAPackage(adapter)
      .should().dependOnClassesThat().resideInAnyPackage(siblings)
      .as(label + " must not depend on other adapters")
      .because("adapters reach the core only through ports; cross-adapter wiring happens in the composition root");
  }

  private static String[] concat(String... rest) {
    var result = new String[COMMON.length + rest.length];
    System.arraycopy(COMMON, 0, result, 0, COMMON.length);
    System.arraycopy(rest, 0, result, COMMON.length, rest.length);
    return result;
  }
}
