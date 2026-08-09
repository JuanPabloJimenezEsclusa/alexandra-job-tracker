package dev.jpje.jobtracker.ai.adapter;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jpje.jobtracker.domain.port.out.JobAnalysisPort;
import dev.jpje.jobtracker.domain.vo.JobAnalysis;
import org.jspecify.annotations.Nullable;
import org.springaicommunity.agent.tools.SkillsTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.DefaultChatOptionsBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class JobAnalyzerAdapter implements JobAnalysisPort {
  private static final String SUMMARY = "summary";
  private static final String SENIORITY = "seniority";
  private static final String FIT_SCORE = "fit_score";
  private static final String TECHNICAL_SKILLS = "technical_skills";
  private static final String SOFT_SKILLS = "soft_skills";
  private static final String COMPANY_RATING = "company_rating";
  private static final String COMPANY_TYPE = "company_type";
  private static final String SALARY_MIN = "salary_min";
  private static final String SALARY_MAX = "salary_max";
  private static final String SALARY_CURRENCY = "salary_currency";

  private final ChatClient chatClient;
  private final Resource promptResource;

  public JobAnalyzerAdapter(final ChatClient.Builder chatClientBuilder,
                            @Value("classpath:prompts/job-analysis.st") final Resource promptResource,
                            @Value("classpath:skills") final Resource skillsRootDirectory) {
    this.chatClient = chatClientBuilder
      .defaultSystem("""
        You are an expert technical recruiter. Output must be valid JSON only.
        All keys must use snake_case.
        """)
      .defaultTools(SkillsTool.builder().addSkillsResource(skillsRootDirectory).build())
      .defaultOptions(new DefaultChatOptionsBuilder<>().temperature(0.1).maxTokens(800))
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
      return new JobAnalysis("Analysis pending", "", List.of(), List.of(), 0.0, 0.0, "unknown", 0.0, 0.0, "USD");
    }
    try {
      final var mapper = new ObjectMapper();
      final var json = mapper.readTree(extractJson(response));
      final var summary = json.has(SUMMARY) ? json.get(SUMMARY).asText("") : "";
      final var seniority = json.has(SENIORITY) ? json.get(SENIORITY).asText("") : "";
      final var softSkills = json.has(SOFT_SKILLS) ? json.get(SOFT_SKILLS).valueStream()
        .map(JsonNode::asText).toList() : List.<String>of();
      final var technicalSkills = json.has(TECHNICAL_SKILLS) ? json.get(TECHNICAL_SKILLS).valueStream()
        .map(JsonNode::asText).toList() : List.<String>of();
      final var fitScore = json.has(FIT_SCORE) ? json.get(FIT_SCORE).asDouble(0.0) : 0.0;
      final var companyRating = json.has(COMPANY_RATING) ? json.get(COMPANY_RATING).asDouble(0.0) : 0.0;
      final var companyType = json.has(COMPANY_TYPE) ? json.get(COMPANY_TYPE).asText("unknown") : "unknown";
      final var salaryMin = json.has(SALARY_MIN) ? json.get(SALARY_MIN).asDouble(0.0) : 0.0;
      final var salaryMax = json.has(SALARY_MAX) ? json.get(SALARY_MAX).asDouble(0.0) : 0.0;
      final var salaryCurrency = json.has(SALARY_CURRENCY) ? json.get(SALARY_CURRENCY).asText("USD") : "USD";

      return new JobAnalysis(summary, seniority, softSkills, technicalSkills, fitScore,
        companyRating, companyType, salaryMin, salaryMax, salaryCurrency);
    } catch (final Exception e) {
      return new JobAnalysis("Parse error: " + e.getMessage(), "", List.of(), List.of(), 0.0,
        0.0, "unknown", 0.0, 0.0, "USD");
    }
  }

  private String extractJson(final String response) {
    final var start = response.indexOf('{');
    final var end = response.lastIndexOf('}');
    if (start == -1 || end <= start) {
      return response;
    }
    return response.substring(start, end + 1);
  }
}
