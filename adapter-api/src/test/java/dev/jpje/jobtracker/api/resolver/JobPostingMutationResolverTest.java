package dev.jpje.jobtracker.api.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import dev.jpje.jobtracker.api.dto.JobAnalysisResponse;
import dev.jpje.jobtracker.api.dto.JobPostingResponse;
import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.port.in.AnalyzeJobPostingPort;
import dev.jpje.jobtracker.domain.port.in.SubmitJobPostingPort;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobAnalysis;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobPostingMutationResolverTest {

  @InjectMocks
  private JobPostingMutationResolver resolver;

  @Mock
  private SubmitJobPostingPort submitUseCase;

  @Mock
  private AnalyzeJobPostingPort analyzeUseCase;

  private static Stream<Arguments> analyzeScenarios() {
    final var jobId = UUID.randomUUID();
    final var analysis = new JobAnalysis("Java role", List.of("Spring", "SQL"), 85.0);
    return Stream.of(
      arguments(jobId, analysis)
    );
  }

  private static Stream<Arguments> analyzeErrorScenarios() {
    final var jobId = UUID.randomUUID();
    return Stream.of(
      arguments(jobId, "Job posting not found")
    );
  }

  @ParameterizedTest(name = "analyze {0}")
  @MethodSource("analyzeScenarios")
  void shouldResolveAnalyze(final UUID jobPostingId, final JobAnalysis analysis) {
    when(analyzeUseCase.analyze(jobPostingId)).thenReturn(analysis);

    assertThat(resolver.analyzeJobPosting(jobPostingId)).isEqualTo(JobAnalysisResponse.from(analysis));

    verify(analyzeUseCase).analyze(jobPostingId);
    verifyNoMoreInteractions(analyzeUseCase);
    verifyNoInteractions(submitUseCase);
  }

  @ParameterizedTest(name = "analyze error {0}")
  @MethodSource("analyzeErrorScenarios")
  void shouldThrowWhenAnalyzeFails(final UUID jobPostingId, final String errorMessage) {
    when(analyzeUseCase.analyze(jobPostingId)).thenThrow(new IllegalArgumentException(errorMessage));

    assertThatThrownBy(() -> resolver.analyzeJobPosting(jobPostingId))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage(errorMessage);

    verifyNoInteractions(submitUseCase);
  }

  @Test
  void shouldSubmitJobPosting() {
    final var userId = new UserId(UUID.randomUUID());
    final var posted = Instancio.of(JobPosting.class)
      .set(field(JobPosting::userId), userId)
      .set(field(JobPosting::source), Source.LINKEDIN)
      .set(field(JobPosting::url), Url.of("https://example.com/job"))
      .set(field(JobPosting::title), JobTitle.of("title"))
      .set(field(JobPosting::company), CompanyName.of("company"))
      .set(field(JobPosting::description), "desc")
      .create();
    final var input = new JobPostingMutationResolver.JobPostingInput("https://example.com/job", "title", "company", "desc", Source.LINKEDIN);

    when(submitUseCase.submit(userId, Url.of("https://example.com/job"), JobTitle.of("title"),
      CompanyName.of("company"), "desc", Source.LINKEDIN)).thenReturn(posted);

    final var result = resolver.submitJobPosting(userId, input);
    assertThat(result)
      .extracting(JobPostingResponse::source, JobPostingResponse::title, JobPostingResponse::company)
      .containsExactly(Source.LINKEDIN, "title", "company");

    verify(submitUseCase).submit(userId, Url.of("https://example.com/job"), JobTitle.of("title"),
      CompanyName.of("company"), "desc", Source.LINKEDIN);
    verifyNoMoreInteractions(submitUseCase);
    verifyNoInteractions(analyzeUseCase);
  }
}
