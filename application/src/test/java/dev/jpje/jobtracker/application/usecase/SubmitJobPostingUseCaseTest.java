package dev.jpje.jobtracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.stream.Stream;

import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.port.outbound.SaveJobPostingPort;
import dev.jpje.jobtracker.domain.service.JobPostingService;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubmitJobPostingUseCaseTest {

  @InjectMocks
  private SubmitJobPostingUseCase useCase;

  @Mock
  private SaveJobPostingPort savePostingPort;

  @Mock
  private JobPostingService jobPostingService;

  @Mock
  private Clock clock;

  private static Stream<Arguments> submitScenarios() {
    return Stream.of(
      arguments(named("LinkedIn", Source.LINKEDIN)),
      arguments(named("Indeed", Source.INDEED)),
      arguments(named("Other", Source.OTHER))
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("submitScenarios")
  void shouldSubmitJobPosting(final Source source) {
    // Given
    final var userId = UserId.generate();
    final var now = Instant.EPOCH;
    final var url = Url.of("https://example.com/job");
    final var title = JobTitle.of("SWE");
    final var company = CompanyName.of("Acme");
    when(clock.instant()).thenReturn(now);

    // When
    final var returned = useCase.submit(userId, url, title, company, "Java developer", source);

    // Then
    assertThat(returned)
      .extracting(JobPosting::userId, JobPosting::url, JobPosting::source,
        JobPosting::title, JobPosting::company, JobPosting::description, JobPosting::postedAt)
      .containsExactly(userId, url, source, title, company, "Java developer", now);
    verify(savePostingPort).save(any(JobPosting.class));
    verify(jobPostingService).submit(any(JobPosting.class));
    verifyNoMoreInteractions(savePostingPort, jobPostingService, clock);
  }
}
