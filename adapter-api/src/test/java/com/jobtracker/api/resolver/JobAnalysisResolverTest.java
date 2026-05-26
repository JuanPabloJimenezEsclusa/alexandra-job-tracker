package com.jobtracker.api.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

class JobAnalysisResolverTest {

  static Stream<Arguments> resolverScenarios() {
    var jobId = UUID.randomUUID();
    var analysis = new JobAnalysis("Java role", List.of("Spring", "SQL"), 85.0);
    return Stream.of(
      Arguments.of(jobId, analysis, null,           "successful analysis"),
      Arguments.of(jobId, null,    "not found",     "posting not found")
    );
  }

  @ParameterizedTest(name = "{3}")
  @MethodSource("resolverScenarios")
  void shouldResolveOrThrow(UUID jobPostingId, JobAnalysis analysis, String errorMessage, String _name) {
    // Given
    var loadPort = mock(LoadJobPostingPort.class);
    var analysisPort = mock(JobAnalysisPort.class);
    var resolver = new JobAnalysisResolver(loadPort, analysisPort);

    if (analysis != null) {
      var userId = UserId.generate();
      when(loadPort.findById(jobPostingId)).thenReturn(Optional.of(
          new JobPosting(jobPostingId, userId, "url", Source.LINKEDIN,
              "SWE", "Acme", "Java role description", null)));
      when(analysisPort.analyze("Java role description")).thenReturn(analysis);
    } else {
      when(loadPort.findById(jobPostingId)).thenReturn(Optional.empty());
    }

    if (errorMessage != null) {
      // When / Then
      assertThatThrownBy(() -> resolver.analyzeJobPosting(jobPostingId))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Job posting not found");
      return;
    }

    // When
    var result = resolver.analyzeJobPosting(jobPostingId);

    // Then
    assertThat(result.summary()).isEqualTo("Java role");
    assertThat(result.skills()).contains("Spring", "SQL");
    assertThat(result.fitScore()).isEqualTo(85.0);
  }
}
