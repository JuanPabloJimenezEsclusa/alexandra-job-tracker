package dev.jpje.jobtracker.ai.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;

import dev.jpje.jobtracker.domain.vo.JobAnalysis;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@ExtendWith(MockitoExtension.class)
class JobAnalyzerAdapterTest {

  private JobAnalyzerAdapter adapter;

  @Mock
  private ChatClient chatClient;

  @Mock
  private ChatClient.Builder builder;

  @Mock
  private ToolCallbackFactory toolCallbackFactory;

  @Mock
  private JobAnalysisParser parser;

  @BeforeEach
  void setUp() {
    when(builder.defaultSystem(any(Resource.class))).thenReturn(builder);
    when(builder.defaultTools(any())).thenReturn(builder);
    when(builder.defaultOptions(any())).thenReturn(builder);
    when(builder.build()).thenReturn(chatClient);
    adapter = new JobAnalyzerAdapter(builder,
      new ClassPathResource("prompts/system-prompt.st"),
      new ClassPathResource("prompts/user-job-analysis.st"),
      toolCallbackFactory,
      parser);
  }

  @Test
  void shouldDelegateParsingToParser() {
    final var analysis = mock(JobAnalysis.class);
    stubChatClient();
    when(parser.parse("{\"summary\":\"Java backend role\"}")).thenReturn(analysis);

    final var result = adapter.analyze("Software Engineer", "Acme", "LINKEDIN",
      "some job description");

    assertThat(result).as("analysis delegated to parser").isEqualTo(analysis);
    verify(parser, description("parser invoked with chat client content")).parse("{\"summary\":\"Java backend role\"}");
  }

  private void stubChatClient() {
    final var requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
    final var callSpec = mock(ChatClient.CallResponseSpec.class);

    when(chatClient.prompt()).thenReturn(requestSpec);
    when(requestSpec.user(any(Consumer.class))).thenReturn(requestSpec);
    when(requestSpec.call()).thenReturn(callSpec);
    when(callSpec.content()).thenReturn("{\"summary\":\"Java backend role\"}");
  }
}
