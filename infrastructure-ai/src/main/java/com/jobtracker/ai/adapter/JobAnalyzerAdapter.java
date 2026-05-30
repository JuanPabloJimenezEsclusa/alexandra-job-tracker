package com.jobtracker.ai.adapter;

import java.util.ArrayList;
import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtracker.domain.model.JobAnalysis;
import com.jobtracker.domain.port.out.JobAnalysisPort;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * Adapter for AI-powered job analysis.
 */
@Component
public class JobAnalyzerAdapter implements JobAnalysisPort {
  private static final String SUMMARY = "summary";
  private static final String FIT_SCORE = "fitScore";
  private static final String SKILLS = "skills";
  private static final String PROMPT = """
    Analyze the following job description and return a JSON with:
    - summary: a brief 1-sentence summary
    - skills: a list of key skills required
    - fitScore: a number from 0.0 to 100.0 estimating how standard the requirements are
    
    JOB DESCRIPTION:
    {description}
    
    Return only valid JSON with keys: summary, skills, fitScore.
    """;
  private final ChatClient chatClient;

  /**
   * JobAnalyzerAdapter.
   */
  public JobAnalyzerAdapter(final ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder.build();
  }

  @Override
  public JobAnalysis analyze(final String jobDescription) {
    final var response = chatClient.prompt()
      .user(u -> u.text(PROMPT)
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
      if (json.has(SKILLS) && json.get(SKILLS).isArray()) {
        json.get(SKILLS).forEach(n -> skills.add(n.asText()));
      }
      return new JobAnalysis(summary, skills, fitScore);
    } catch (Exception e) {
      return new JobAnalysis("Parse error: " + e.getMessage(), Collections.emptyList(), 0.0);
    }
  }
}
