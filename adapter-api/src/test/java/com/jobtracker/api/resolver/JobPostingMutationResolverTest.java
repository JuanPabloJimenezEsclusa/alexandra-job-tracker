package com.jobtracker.api.resolver;

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

import com.jobtracker.domain.model.JobAnalysis;
import com.jobtracker.domain.model.JobPosting;
import com.jobtracker.domain.port.in.AnalyzeJobPostingUseCase;
import com.jobtracker.domain.port.in.SubmitJobPostingUseCase;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import org.instancio.Instancio;
import org.jspecify.annotations.Nullable;
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
  private SubmitJobPostingUseCase submitUseCase;

  @Mock
  private AnalyzeJobPostingUseCase analyzeUseCase;

  private static Stream<Arguments> analyzeScenarios() {
    final var jobId = UUID.randomUUID();
    final var analysis = new JobAnalysis("Java role", List.of("Spring", "SQL"), 85.0);
    return Stream.of(
      arguments(jobId, analysis, null),
      arguments(jobId, null, "Job posting not found")
    );
  }

  @ParameterizedTest(name = "analyze scenario {index}")
  @MethodSource("analyzeScenarios")
  void shouldResolveAnalyzeOrThrow(final UUID jobPostingId,
                                   final JobAnalysis analysis,
                                   @Nullable final String errorMessage) {
    if (errorMessage != null) {
      when(analyzeUseCase.analyze(jobPostingId)).thenThrow(new IllegalArgumentException(errorMessage));
      assertThatThrownBy(() -> resolver.analyzeJobPosting(jobPostingId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(errorMessage);
      return;
    }

    when(analyzeUseCase.analyze(jobPostingId)).thenReturn(analysis);

    assertThat(resolver.analyzeJobPosting(jobPostingId))
      .isNotNull()
      .extracting(JobAnalysis::summary, JobAnalysis::skills, JobAnalysis::fitScore)
      .containsExactly("Java role", List.of("Spring", "SQL"), 85.0);

    verify(analyzeUseCase).analyze(jobPostingId);
    verifyNoMoreInteractions(analyzeUseCase);
    verifyNoInteractions(submitUseCase);
  }

  @Test
  void shouldSubmitJobPosting() {
    final var userId = new UserId(UUID.randomUUID());
    final var posted = Instancio.of(JobPosting.class)
      .set(field(JobPosting::userId), userId)
      .set(field(JobPosting::source), Source.LINKEDIN)
      .set(field(JobPosting::url), "url")
      .set(field(JobPosting::title), "title")
      .set(field(JobPosting::company), "company")
      .set(field(JobPosting::description), "desc")
      .create();
    final var input = new JobPostingMutationResolver.JobPostingInput("url", "title", "company", "desc", Source.LINKEDIN);

    when(submitUseCase.submit(userId, "url", "title", "company", "desc", Source.LINKEDIN)).thenReturn(posted);

    assertThat(resolver.submitJobPosting(userId, input)).isEqualTo(posted);

    verify(submitUseCase).submit(userId, "url", "title", "company", "desc", Source.LINKEDIN);
    verifyNoMoreInteractions(submitUseCase);
    verifyNoInteractions(analyzeUseCase);
  }
}
