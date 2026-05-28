package dev.jpje.jobtracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.port.out.JobAnalysisPort;
import dev.jpje.jobtracker.domain.port.out.LoadJobPostingPort;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobAnalysis;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AnalyzeJobPostingUseCaseTest {

  private static Stream<Arguments> analysisScenarios() {
    var userId = UserId.generate();
    var posting = Instancio.of(JobPosting.class)
      .set(field(JobPosting::userId), userId)
      .set(field(JobPosting::source), Source.LINKEDIN)
      .set(field(JobPosting::url), Url.of("https://example.com/job"))
      .set(field(JobPosting::title), JobTitle.of("Software Engineer"))
      .set(field(JobPosting::company), CompanyName.of("Acme"))
      .set(field(JobPosting::description), "We need a Java developer with Spring experience")
      .create();
    var analysis = new JobAnalysis("Java role", List.of("Java", "Spring"), 90.0);
    return Stream.of(
      arguments(named("found posting", posting), analysis)
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("analysisScenarios")
  void shouldAnalyzePosting(final JobPosting posting, final JobAnalysis expectedAnalysis) {
    // Given
    final var loadPort = mock(LoadJobPostingPort.class);
    final var analysisPort = mock(JobAnalysisPort.class);
    final var useCase = new AnalyzeJobPostingUseCase(loadPort, analysisPort);

    when(loadPort.findById(posting.id())).thenReturn(Optional.of(posting));
    when(analysisPort.analyze(posting.description())).thenReturn(expectedAnalysis);

    // When, then
    assertThat(useCase.analyze(posting.id())).isEqualTo(expectedAnalysis);
  }

  @Test
  void shouldThrowWhenPostingNotFound() {
    // Given
    final var loadPort = mock(LoadJobPostingPort.class);
    final var analysisPort = mock(JobAnalysisPort.class);
    final var useCase = new AnalyzeJobPostingUseCase(loadPort, analysisPort);
    final var randomUUID = UUID.randomUUID();

    when(loadPort.findById(randomUUID)).thenReturn(Optional.empty());

    // When, then
    assertThatThrownBy(() -> useCase.analyze(randomUUID))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Job posting not found");
  }
}
