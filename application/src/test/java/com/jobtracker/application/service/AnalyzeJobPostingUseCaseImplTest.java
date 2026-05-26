package com.jobtracker.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import com.jobtracker.domain.model.JobAnalysis;
import com.jobtracker.domain.model.JobPosting;
import com.jobtracker.domain.port.out.JobAnalysisPort;
import com.jobtracker.domain.port.out.LoadJobPostingPort;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AnalyzeJobPostingUseCaseImplTest {

  static Stream<Arguments> analysisScenarios() {
    var jobPostingId = UUID.randomUUID();
    var userId = UserId.generate();
    var posting = new JobPosting(jobPostingId, userId, "https://example.com/job",
        Source.LINKEDIN, "Software Engineer", "Acme",
        "We need a Java developer with Spring experience", Instant.now());
    var analysis = new JobAnalysis("Java role", List.of("Java", "Spring"), 90.0);
    return Stream.of(
      Arguments.of(posting, analysis, "found posting"),
      Arguments.of(null,    null,     "missing posting")
    );
  }

  @ParameterizedTest(name = "{2}")
  @MethodSource("analysisScenarios")
  void shouldAnalyzeOrThrow(JobPosting posting, JobAnalysis expectedAnalysis, String _name) {
    // Given
    var loadPort = mock(LoadJobPostingPort.class);
    var analysisPort = mock(JobAnalysisPort.class);
    var useCase = new AnalyzeJobPostingUseCaseImpl(loadPort, analysisPort);

    if (posting != null) {
      when(loadPort.findById(posting.id())).thenReturn(Optional.of(posting));
      when(analysisPort.analyze(posting.description())).thenReturn(expectedAnalysis);
    } else {
      when(loadPort.findById(UUID.randomUUID())).thenReturn(Optional.empty());
    }

    if (posting == null) {
      // When / Then
      assertThatThrownBy(() -> useCase.analyze(UUID.randomUUID()))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Job posting not found");
      return;
    }

    // When
    var result = useCase.analyze(posting.id());

    // Then
    assertThat(result).isEqualTo(expectedAnalysis);
    assertThat(result.summary()).isEqualTo("Java role");
    assertThat(result.skills()).contains("Java", "Spring");
    assertThat(result.fitScore()).isEqualTo(90.0);
  }
}
