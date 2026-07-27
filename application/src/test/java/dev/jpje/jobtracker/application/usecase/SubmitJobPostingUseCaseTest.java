package dev.jpje.jobtracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.port.out.SaveJobApplicationPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobPostingPort;
import dev.jpje.jobtracker.domain.service.JobPostingService;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.RoleName;
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
  private SaveJobApplicationPort saveAppPort;

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
    final var userId = UserId.generate();
    final var now = Instant.EPOCH;
    final var url = Url.of("https://example.com/job");
    final var title = JobTitle.of("SWE");
    final var company = CompanyName.of("Acme");
    when(clock.instant()).thenReturn(now);

    final var posting = new JobPosting(UUID.randomUUID(), userId, url, source,
      title, company, "Java developer", now);
    final var tracking = new JobApplication(UUID.randomUUID(), userId, company,
      RoleName.of(title.value()), source, url, ApplicationStatus.SAVED, now, now, null);
    final var result = new JobPostingService.SubmittedPosting(posting, tracking);
    when(jobPostingService.submit(userId, url, title, company, "Java developer", source, now))
      .thenReturn(result);

    final var returned = useCase.submit(userId, url, title, company, "Java developer", source);

    assertThat(returned).isEqualTo(posting);
    verify(savePostingPort).save(result.posting());
    verify(saveAppPort).save(result.tracking());
  }
}
