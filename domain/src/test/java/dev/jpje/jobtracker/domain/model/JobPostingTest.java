package dev.jpje.jobtracker.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
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
      arguments(named("null id", null), uid, Url.of("https://example.com"), Source.LINKEDIN,
        JobTitle.of("t"), CompanyName.of("c"), "d", now),
      arguments(named("null userId", id), null, Url.of("https://example.com"), Source.LINKEDIN,
        JobTitle.of("t"), CompanyName.of("c"), "d", now),
      arguments(named("null source", id), uid, Url.of("https://example.com"), null,
        JobTitle.of("t"), CompanyName.of("c"), "d", now),
      arguments(named("null postedAt", id), uid, Url.of("https://example.com"), Source.LINKEDIN,
        JobTitle.of("t"), CompanyName.of("c"), "d", null),
      arguments(named("blank description", id), uid, Url.of("https://example.com"), Source.LINKEDIN,
        JobTitle.of("t"), CompanyName.of("c"), "", now)
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidInputs")
  void shouldRejectInvalidInputs(final Object id, final UserId userId, final Url url,
                                   final Source source, final JobTitle title, final CompanyName company,
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
    assertThat(new JobPosting(id, uid, Url.of("https://example.com"), Source.LINKEDIN,
      JobTitle.of("title"), CompanyName.of("company"), "desc", now))
      .as("posting id").returns(id, JobPosting::id)
      .as("user id").returns(uid, JobPosting::userId)
      .as("url").returns(Url.of("https://example.com"), JobPosting::url)
      .as("source").returns(Source.LINKEDIN, JobPosting::source)
      .as("title").returns(JobTitle.of("title"), JobPosting::title)
      .as("company").returns(CompanyName.of("company"), JobPosting::company)
      .as("description").returns("desc", JobPosting::description)
      .as("posted at").returns(now, JobPosting::postedAt);
  }
}
