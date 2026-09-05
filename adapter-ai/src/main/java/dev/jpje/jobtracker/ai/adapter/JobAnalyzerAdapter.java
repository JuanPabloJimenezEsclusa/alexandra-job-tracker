package dev.jpje.jobtracker.ai.adapter;

import dev.jpje.jobtracker.domain.port.outbound.JobAnalysisPort;
import dev.jpje.jobtracker.domain.vo.JobAnalysis;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.DefaultChatOptionsBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class JobAnalyzerAdapter implements JobAnalysisPort {
  private final ChatClient chatClient;
  private final Resource promptResource;
  private final JobAnalysisParser parser;

  public JobAnalyzerAdapter(final ChatClient.Builder chatClientBuilder,
                            @Value("classpath:prompts/system-prompt.st") final Resource systemPromptResource,
                            @Value("classpath:prompts/user-job-analysis.st") final Resource promptResource,
                            final ToolCallbackFactory toolCallbackFactory,
                            final JobAnalysisParser parser) {
    this.chatClient = chatClientBuilder
      .defaultSystem(systemPromptResource)
      .defaultTools(toolCallbackFactory.createToolCallback())
      .defaultOptions(new DefaultChatOptionsBuilder<>().temperature(0.1).maxTokens(800))
      .build();
    this.promptResource = promptResource;
    this.parser = parser;
  }

  @Override
  public JobAnalysis analyze(final String title, final String company,
                             final String source, final String jobDescription) {
    final var response = chatClient.prompt()
      .user(u -> u.text(promptResource)
        .param("title", title)
        .param("company", company)
        .param("source", source)
        .param("description", jobDescription))
      .call()
      .content();
    return parser.parse(response);
  }
}
