package dev.jpje.jobtracker.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import dev.jpje.jobtracker.api.config.IntegrationTestConfig;
import dev.jpje.jobtracker.server.JobTrackerServerApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

@SpringBootTest(
  classes = JobTrackerServerApplication.class,
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfig.class)
class ExposureIntegrationTest extends GraphQlIntegrationTestBase {

  private static Stream<Arguments> endpointExposureScenarios() {
    return Stream.of(
      arguments(named("health actuator", "/actuator/health"), HttpStatus.OK.value()),
      arguments(named("env actuator", "/actuator/env"), HttpStatus.NOT_FOUND.value()),
      arguments(named("graphiql", "/graphiql"), HttpStatus.NOT_FOUND.value())
    );
  }

  @ParameterizedTest(name = "{0} is served as HTTP {1} in the default profile")
  @MethodSource("endpointExposureScenarios")
  void shouldExposeOnlySafeEndpointsInDefaultProfile(final String path, final int httpStatus) {
    // When
    final var status = rest.get()
      .uri(apiBaseUrl() + path)
      .exchange((_, response) -> response.getStatusCode().value());

    // Then
    assertThat(status)
      .as("endpoint %s must be served as HTTP %s in the default profile", path, httpStatus)
      .isEqualTo(httpStatus);
  }

  @Test
  void shouldRejectDisallowedCrossOrigin() {
    // When
    final var headers = rest.get()
      .uri(url())
      .header("Origin", "http://evil.example")
      .exchange((_, response) -> response.getHeaders());

    // Then
    assertThat(headers.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
      .as("non-configured origin must not receive CORS headers")
      .isNull();
  }

  private String apiBaseUrl() {
    return url().replace("/graphql", "");
  }
}
