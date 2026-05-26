package com.jobtracker.ai.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.ai.chat.client.ChatClient;

class JobAnalyzerAdapterTest {

  static Stream<Arguments> analysisResponses() {
    return Stream.of(
      Arguments.of("We need Java skills",  "java role",    "analysis with description"),
      Arguments.of("",                      "unknown role", "empty description"),
      Arguments.of("Must know Spring Boot", "spring role",  "tech description")
    );
  }

  @ParameterizedTest(name = "{2}")
  @MethodSource("analysisResponses")
  void shouldAnalyzeJobDescription(String description, String expectedSummaryPrefix, String _name) {
    // Given
    var chatClient = mockChatClient("{\"summary\":\"test\",\"skills\":[\"Java\"],\"fitScore\":75.0}");
    var adapter = new JobAnalyzerAdapter(chatClient, true);

    // When
    var result = adapter.analyze(description);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.skills()).contains("Java");
    assertThat(result.fitScore()).isEqualTo(75.0);
  }

  private ChatClient mockChatClient(String responseContent) {
    var callSpec = mock(ChatClient.CallResponseSpec.class);
    when(callSpec.content()).thenReturn(responseContent);

    var requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
    when(requestSpec.user(any(java.util.function.Consumer.class))).thenReturn(requestSpec);
    when(requestSpec.call()).thenReturn(callSpec);

    var chatClient = mock(ChatClient.class);
    when(chatClient.prompt()).thenReturn(requestSpec);

    return chatClient;
  }
}
