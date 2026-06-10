package com.jobtracker.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.stream.Stream;

import com.jobtracker.domain.port.out.SaveJobApplicationPort;
import com.jobtracker.domain.port.out.SaveJobPostingPort;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubmitJobPostingUseCaseImplTest {

  @InjectMocks
  private SubmitJobPostingUseCaseImpl useCase;

  @Mock
  private SaveJobPostingPort savePostingPort;

  @Mock
  private SaveJobApplicationPort saveAppPort;

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
    when(clock.instant()).thenReturn(Instant.EPOCH);

    // When
    final var posting = useCase.submit(userId, "https://example.com/job",
      "SWE", "Acme", "Java developer", source);

    // Then
    assertThat(posting.title()).isEqualTo("SWE");
    assertThat(posting.company()).isEqualTo("Acme");
    assertThat(posting.source()).isEqualTo(source);
    verify(savePostingPort).save(posting);
    verify(saveAppPort).save(any());
  }
}
