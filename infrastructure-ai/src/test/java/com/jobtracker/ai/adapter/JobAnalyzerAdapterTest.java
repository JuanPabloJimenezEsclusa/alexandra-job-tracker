package com.jobtracker.ai.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ClassPathResource;

@ExtendWith(MockitoExtension.class)
class JobAnalyzerAdapterTest {

  private JobAnalyzerAdapter adapter;

  @Mock
  private ChatClient chatClient;

  @Mock
  private ChatClient.Builder builder;

  private static Stream<Arguments> validResponses() {
    return Stream.of(
      arguments(named("valid response with all fields",
        """
        {
          "summary": "Java backend role",
          "technical_skills": ["Java", "Spring"],
          "soft_skills": ["Teamwork"],
          "fit_score": 85.0,
          "seniority": "senior"
        }
        """),
        "Java backend role",
        List.of("Java", "Spring", "Teamwork"),
        85.0),
      arguments(named("partial JSON with only summary and fit_score",
        """
        {
          "summary": "Only summary provided",
          "fit_score": 50.0
        }
        """),
        "Only summary provided",
        List.of(),
        50.0),
      arguments(named("merge technical and soft skills",
        """
        {
          "summary": "Full stack role",
          "technical_skills": ["Java", "Spring", "React", "AWS"],
          "soft_skills": ["Communication", "Leadership"],
          "fit_score": 90.0,
          "seniority": "senior"
        }
        """),
        "Full stack role",
        List.of("Java", "Spring", "React", "AWS", "Communication", "Leadership"),
        90.0),
      arguments(named("only technical skills",
        """
        {
          "summary": "Backend role",
          "technical_skills": ["Go", "Postgres"],
          "soft_skills": [],
          "fit_score": 70.0,
          "seniority": "mid"
        }
        """),
        "Backend role",
        List.of("Go", "Postgres"),
        70.0),
      arguments(named("only soft skills",
        """
        {
          "summary": "People role",
          "technical_skills": [],
          "soft_skills": ["Communication", "Empathy"],
          "fit_score": 40.0,
          "seniority": "lead"
        }
        """),
        "People role",
        List.of("Communication", "Empathy"),
        40.0)
    );
  }

  private static Stream<Arguments> invalidResponses() {
    return Stream.of(
      arguments(named("null response", null), "Analysis pending"),
      arguments(named("empty response", ""), "Analysis pending"),
      arguments(named("malformed JSON", "not valid json at all"), "Parse error")
    );
  }

  @BeforeEach
  void setUp() {
    when(builder.defaultSystem(anyString())).thenReturn(builder);
    when(builder.defaultOptions(any())).thenReturn(builder);
    when(builder.build()).thenReturn(chatClient);
    adapter = new JobAnalyzerAdapter(builder, new ClassPathResource("prompts/job-analysis.st"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("validResponses")
  void shouldParseResponse(final String response,
                           final String expectedSummary,
                           final List<String> expectedSkills,
                           final double expectedFitScore) {
    // Given
    stubChatClient(response);

    // When
    final var result = adapter.analyze("some job description");

    // Then
    assertThat(result.summary()).isEqualTo(expectedSummary);
    assertThat(result.skills()).containsExactlyElementsOf(expectedSkills);
    assertThat(result.fitScore()).isEqualTo(expectedFitScore);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidResponses")
  void shouldReturnFallbackForInvalidResponse(final String response,
                                              final String expectedSummaryPrefix) {
    // Given
    stubChatClient(response);

    // When
    final var result = adapter.analyze("any description");

    // Then
    assertThat(result.summary()).startsWith(expectedSummaryPrefix);
    assertThat(result.skills()).isEmpty();
    assertThat(result.fitScore()).isZero();
  }

  private void stubChatClient(final String response) {
    var requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
    var callSpec = mock(ChatClient.CallResponseSpec.class);

    when(chatClient.prompt()).thenReturn(requestSpec);
    when(requestSpec.user(any(Consumer.class))).thenReturn(requestSpec);
    when(requestSpec.call()).thenReturn(callSpec);
    when(callSpec.content()).thenReturn(response);
  }
}
