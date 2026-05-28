package dev.jpje.jobtracker.api.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.IOException;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.PropertyPlaceholderHelper;

class ProfileConfigurationTest {

  private static final String PROFILE_KEY = "spring.config.activate.on-profile";
  private static final String DATASOURCE_URL_KEY = "spring.datasource.url";
  private static final String DATASOURCE_DRIVER_KEY = "spring.datasource.driver-class-name";
  private static final String POSTGRES_URL_PREFIX = "jdbc:postgresql";
  private static final String H2_FILE_URL_PREFIX = "jdbc:h2:file";
  private static final String POSTGRES_DRIVER = "org.postgresql.Driver";
  private static final String H2_DRIVER = "org.h2.Driver";
  private static final PropertyPlaceholderHelper PLACEHOLDER_HELPER =
    new PropertyPlaceholderHelper("${", "}", ":", null, false);

  private static Stream<Arguments> profileDatasources() {
    return Stream.of(
      arguments(named("dev targets PostgreSQL", "dev"), POSTGRES_URL_PREFIX, POSTGRES_DRIVER),
      arguments(named("loc targets file-backed H2", "loc"), H2_FILE_URL_PREFIX, H2_DRIVER));
  }

  @ParameterizedTest
  @MethodSource("profileDatasources")
  void shouldResolveProfileDatasource(final String profile, final String expectedUrlPrefix,
                                      final String expectedDriver) throws IOException {
    // Given
    final var document = profileDocument(profile);

    // When
    final var url = resolvedProperty(document);

    // Then
    assertThat(url).as("%s datasource url", profile).startsWith(expectedUrlPrefix);
    assertThat(document.getProperty(DATASOURCE_DRIVER_KEY)).as("%s datasource driver", profile)
      .isEqualTo(expectedDriver);
  }

  private static PropertySource<?> profileDocument(final String profile) throws IOException {
    final var loader = new YamlPropertySourceLoader();
    final var documents = loader.load("application", new ClassPathResource("application.yml"));
    return documents.stream()
      .filter(document -> profile.equals(document.getProperty(PROFILE_KEY)))
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("No application.yml document for profile " + profile));
  }

  private static String resolvedProperty(final PropertySource<?> document) {
    final var value = document.getProperty(ProfileConfigurationTest.DATASOURCE_URL_KEY);
    assertThat(value).as("property %s is present", ProfileConfigurationTest.DATASOURCE_URL_KEY).isNotNull();
    return PLACEHOLDER_HELPER.replacePlaceholders(value.toString(), _ -> null);
  }
}
