package com.jobtracker.domain.arch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import java.util.stream.Stream;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class HexagonalArchitectureTest {

  static Stream<ArchRule> domainIndependenceRules() {
    return Stream.of(
      noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
        .as("domain must not depend on Spring"),
      noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat().resideInAnyPackage(
          "..persistence..",
          "..scraping..",
          "..ai..",
          "..auth..")
        .as("domain must not depend on infrastructure")
    );
  }

  static Stream<ArchRule> hexagonalLayerRules() {
    return Stream.of(
      noClasses()
        .that().resideInAPackage("..application..")
        .should().dependOnClassesThat().resideInAnyPackage("..adapter..", "..bootstrap..")
        .allowEmptyShould(true),
      noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat().resideInAnyPackage(
          "..application..",
          "..adapter..",
          "..bootstrap..",
          "..persistence..",
          "..scraping..",
          "..ai..",
          "..auth..",
          "..observability..")
        .allowEmptyShould(true)
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("domainIndependenceRules")
  void shouldEnforceDomainIndependence(final ArchRule rule) {
    rule.check(new ClassFileImporter().importPackages("com.jobtracker"));
  }

  @ParameterizedTest(name = "application layer must not depend on adapters or bootstrap")
  @MethodSource("hexagonalLayerRules")
  void shouldEnforceHexagonalLayers(final ArchRule rule) {
    rule.check(new ClassFileImporter().importPackages("com.jobtracker"));
  }
}
