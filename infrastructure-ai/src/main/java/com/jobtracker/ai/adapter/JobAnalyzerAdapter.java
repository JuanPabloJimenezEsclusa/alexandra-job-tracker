package com.jobtracker.ai.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtracker.domain.model.JobAnalysis;
import com.jobtracker.domain.port.out.JobAnalysisPort;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class JobAnalyzerAdapter implements JobAnalysisPort {
  private final ChatClient chatClient;

  public JobAnalyzerAdapter(ChatClient.Builder builder) {
    this.chatClient = builder.build();
  }

  // Used by tests to inject a mock ChatClient directly
  JobAnalyzerAdapter(ChatClient chatClient, boolean _useDirect) {
    this.chatClient = chatClient;
  }

  @Override
  public JobAnalysis analyze(String jobDescription) {
    var response = chatClient.prompt()
      .user(u -> u.text("""
          Analyze the following job description and return a JSON with:
          - summary: a brief 1-sentence summary
          - skills: a list of key skills required
          - fitScore: a number from 0.0 to 100.0 estimating how standard the requirements are

          JOB DESCRIPTION:
          {description}

          Return only valid JSON with keys: summary, skills, fitScore.
          """)
        .param("description", jobDescription))
      .call()
      .content();
    return parseResponse(response);
  }

  private JobAnalysis parseResponse(String response) {
    if (response == null || response.isBlank()) {
      return new JobAnalysis("Analysis pending", Collections.emptyList(), 0.0);
    }
    try {
      var mapper = new ObjectMapper();
      var json = mapper.readTree(response);
      var summary = json.has("summary") ? json.get("summary").asText("") : "";
      var fitScore = json.has("fitScore") ? json.get("fitScore").asDouble(0.0) : 0.0;
      var skills = new ArrayList<String>();
      if (json.has("skills") && json.get("skills").isArray()) {
        json.get("skills").forEach(n -> skills.add(n.asText()));
      }
      return new JobAnalysis(summary, skills, fitScore);
    } catch (Exception e) {
      return new JobAnalysis("Parse error: " + e.getMessage(), Collections.emptyList(), 0.0);
    }
  }
}
