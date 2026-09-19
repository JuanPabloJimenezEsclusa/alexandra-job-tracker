package dev.jpje.jobtracker.architecture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

class PortPlacementGuardTest {

  private static final String DOMAIN_OUTBOUND_PORTS = "dev.jpje.jobtracker.domain.port.outbound";
  private static final String DOMAIN_EVENTS = "dev.jpje.jobtracker.domain.event";

  @Test
  void shouldMoveTheCacheContractIntoTheCacheAdapter() throws ClassNotFoundException {
    assertThatThrownBy(() -> Class.forName("dev.jpje.jobtracker.domain.port.outbound.CachePort"))
      .isInstanceOf(ClassNotFoundException.class);
    final var cachePort = Class.forName("dev.jpje.jobtracker.cache.port.CachePort");
    assertThat(cachePort.isInterface()).as("relocated cache port is an interface").isTrue();
    assertThat(cachePort.getDeclaredMethods())
      .extracting(Method::getName)
      .containsExactlyInAnyOrder("get", "put", "evict", "clear");
  }

  @Test
  void shouldMoveJobPostingServiceIntoTheApplicationServicePackage() throws ClassNotFoundException {
    assertThatThrownBy(() -> Class.forName("dev.jpje.jobtracker.domain.service.JobPostingService"))
      .isInstanceOf(ClassNotFoundException.class);
    final var service = Class.forName("dev.jpje.jobtracker.application.service.JobPostingService");
    assertThat(service.isInterface()).as("relocated job posting service is a class").isFalse();
    assertThat(service.getDeclaredMethods())
      .extracting(Method::getName)
      .contains("submit");
  }

  @Test
  void shouldMoveTheTechnicalOutboundPortsAndEventPublisherIntoTheApplicationCore() throws ClassNotFoundException {
    assertThatThrownBy(() -> Class.forName("dev.jpje.jobtracker.domain.port.outbound.PasswordEncoderPort"))
      .isInstanceOf(ClassNotFoundException.class);
    assertThatThrownBy(() -> Class.forName("dev.jpje.jobtracker.domain.port.outbound.TokenGeneratorPort"))
      .isInstanceOf(ClassNotFoundException.class);
    assertThatThrownBy(() -> Class.forName("dev.jpje.jobtracker.domain.port.outbound.AuthenticateUserPort"))
      .isInstanceOf(ClassNotFoundException.class);
    assertThatThrownBy(() -> Class.forName("dev.jpje.jobtracker.domain.event.EventPublisher"))
      .isInstanceOf(ClassNotFoundException.class);
    assertThat(Class.forName("dev.jpje.jobtracker.application.port.outbound.PasswordEncoderPort").isInterface())
      .as("password encoder port is an interface in the application core").isTrue();
    assertThat(Class.forName("dev.jpje.jobtracker.application.port.outbound.TokenGeneratorPort").isInterface())
      .as("token generator port is an interface in the application core").isTrue();
    assertThat(Class.forName("dev.jpje.jobtracker.application.port.outbound.AuthenticateUserPort").isInterface())
      .as("authenticate user port is an interface in the application core").isTrue();
    assertThat(Class.forName("dev.jpje.jobtracker.application.port.outbound.EventPublisher").isInterface())
      .as("event publisher port is an interface in the application core").isTrue();
  }

  @Test
  void shouldPinTheDomainOutboundPorts() {
    assertThat(importPackage(DOMAIN_OUTBOUND_PORTS))
      .extracting(JavaClass::getSimpleName)
      .containsExactlyInAnyOrder(
        "JobAnalysisPort",
        "LoadJobAnalysisPort",
        "LoadJobApplicationPort",
        "LoadJobPostingPort",
        "LoadUserPort",
        "SaveJobAnalysisPort",
        "SaveJobApplicationPort",
        "SaveJobPostingPort",
        "SaveUserPort");
  }

  @Test
  void shouldPinTheDomainEvents() {
    assertThat(importPackage(DOMAIN_EVENTS))
      .extracting(JavaClass::getSimpleName)
      .containsExactlyInAnyOrder(
        "DomainEvent",
        "JobApplicationStatusChanged",
        "JobPostingCreated",
        "UserRegistered");
  }

  @Test
  void shouldPinThePasswordEncoderPortRoleSurface() throws ClassNotFoundException {
    assertThat(Class.forName("dev.jpje.jobtracker.application.port.outbound.PasswordEncoderPort").getDeclaredMethods())
      .extracting(Method::getName)
      .containsExactly("encode");
  }

  private static JavaClasses importPackage(final String packageName) {
    return new ClassFileImporter()
      .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_PACKAGE_INFOS)
      .importPackages(packageName);
  }
}
