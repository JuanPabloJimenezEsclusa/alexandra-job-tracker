package dev.jpje.jobtracker.ai.adapter;

import java.util.ArrayList;
import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jpje.jobtracker.domain.model.JobAnalysis;
import dev.jpje.jobtracker.domain.port.out.JobAnalysisPort;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.DefaultChatOptionsBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class JobAnalyzerAdapter implements JobAnalysisPort {
  private static final String SUMMARY = "summary";
  private static final String FIT_SCORE = "fit_score";
  private static final String TECHNICAL_SKILLS = "technical_skills";
  private static final String SOFT_SKILLS = "soft_skills";

  private final ChatClient chatClient;
  private final Resource promptResource;

  public JobAnalyzerAdapter(final ChatClient.Builder chatClientBuilder,
                            @Value("classpath:prompts/job-analysis.st") final Resource promptResource) {
    this.chatClient = chatClientBuilder
      .defaultSystem("""
        You are an expert technical recruiter. Output must be valid JSON only.
        All keys must use snake_case.
        """)
      .defaultOptions(new DefaultChatOptionsBuilder<>()
        .temperature(0.1)
        .maxTokens(800))
      .build();
    this.promptResource = promptResource;
  }

  @Override
  public JobAnalysis analyze(final String jobDescription) {
    final var response = chatClient.prompt()
      .user(u -> u.text(promptResource)
        .param("description", jobDescription))
      .call()
      .content();
    return parseResponse(response);
  }

  private JobAnalysis parseResponse(@Nullable final String response) {
    if (response == null || response.isBlank()) {
      return new JobAnalysis("Analysis pending", Collections.emptyList(), 0.0);
    }
    try {
      final var mapper = new ObjectMapper();
      final var json = mapper.readTree(response);
      final var summary = json.has(SUMMARY) ? json.get(SUMMARY).asText("") : "";
      final var fitScore = json.has(FIT_SCORE) ? json.get(FIT_SCORE).asDouble(0.0) : 0.0;
      final var skills = new ArrayList<String>();
      if (json.has(TECHNICAL_SKILLS) && json.get(TECHNICAL_SKILLS).isArray()) {
        json.get(TECHNICAL_SKILLS).forEach(n -> skills.add(n.asText()));
      }
      if (json.has(SOFT_SKILLS) && json.get(SOFT_SKILLS).isArray()) {
        json.get(SOFT_SKILLS).forEach(n -> skills.add(n.asText()));
      }
      return new JobAnalysis(summary, skills, fitScore);
    } catch (final Exception e) {
      return new JobAnalysis("Parse error: " + e.getMessage(), Collections.emptyList(), 0.0);
    }
  }
}
