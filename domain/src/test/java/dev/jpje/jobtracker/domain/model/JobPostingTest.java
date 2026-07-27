package dev.jpje.jobtracker.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JobPostingTest {

  private static Stream<Arguments> invalidInputs() {
    final var id = UUID.randomUUID();
    final var uid = UserId.generate();
    final var now = Instant.EPOCH;
    return Stream.of(
      arguments(named("null id", null), uid, "url", Source.LINKEDIN, "t", "c", "d", now),
      arguments(named("null userId", id), null, "url", Source.LINKEDIN, "t", "c", "d", now),
      arguments(named("null source", id), uid, "url", null, "t", "c", "d", now),
      arguments(named("null postedAt", id), uid, "url", Source.LINKEDIN, "t", "c", "d", null),
      arguments(named("blank url", id), uid, "  ", Source.LINKEDIN, "t", "c", "d", now),
      arguments(named("blank title", id), uid, "url", Source.LINKEDIN, "", "c", "d", now),
      arguments(named("blank description", id), uid, "url", Source.LINKEDIN, "t", "c", "", now),
      arguments(named("blank company", id), uid, "url", Source.LINKEDIN, "t", "  ", "d", now)
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidInputs")
  void shouldRejectInvalidInputs(final Object id, final UserId userId, final String url,
                                   final Source source, final String title, final String company,
                                   final String description, final Instant postedAt) {
    assertThatThrownBy(() -> new JobPosting((UUID) id, userId, url, source, title, company, description, postedAt))
      .isInstanceOf(RuntimeException.class);
  }

  @Test
  void shouldCreateValidPosting() {
    // Given
    final var id = UUID.randomUUID();
    final var uid = UserId.generate();
    final var now = Instant.EPOCH;

    // When, then
    assertThat(new JobPosting(id, uid, "url", Source.LINKEDIN, "title", "company", "desc", now))
      .returns(id, JobPosting::id)
      .returns(uid, JobPosting::userId)
      .returns("url", JobPosting::url)
      .returns(Source.LINKEDIN, JobPosting::source)
      .returns("title", JobPosting::title)
      .returns("company", JobPosting::company)
      .returns("desc", JobPosting::description)
      .returns(now, JobPosting::postedAt);
  }
}
