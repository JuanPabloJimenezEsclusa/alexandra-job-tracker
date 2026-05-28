package dev.jpje.jobtracker.ai.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;

import dev.jpje.jobtracker.domain.vo.JobAnalysis;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JobAnalysisParserTest {

  private static final double FALLBACK_FIT_SCORE = 0.0;
  private static final double FALLBACK_COMPANY_RATING = 0.0;
  private static final double FALLBACK_SALARY_MIN = 0.0;
  private static final double FALLBACK_SALARY_MAX = 0.0;

  private final JobAnalysisParser parser = new JobAnalysisParser();

  private static Stream<Arguments> validResponses() {
    return Stream.of(
      arguments(named("valid response with all fields",
        """
        {
          "summary": "Java backend role",
          "technical_skills": ["Java", "Spring"],
          "soft_skills": ["Teamwork"],
          "fit_score": 85.0,
          "seniority": "senior",
          "company_rating": 4.2,
          "company_type": "enterprise",
          "salary_min": 90000.0,
          "salary_max": 130000.0,
          "salary_currency": "USD"
        }
        """),
        "Java backend role",
        "senior",
        List.of("Teamwork"),
        List.of("Java", "Spring"),
        85.0, 4.2, "enterprise", 90000.0, 130000.0, "USD"),
      arguments(named("partial JSON with only summary and fit_score",
        """
        {
          "summary": "Only summary provided",
          "fit_score": 50.0
        }
        """),
        "Only summary provided",
        "",
        List.of(),
        List.of(),
        50.0, 0.0, "unknown", 0.0, 0.0, "USD"),
      arguments(named("merge technical and soft skills",
        """
        {
          "summary": "Full stack role",
          "technical_skills": ["Java", "Spring", "React", "AWS"],
          "soft_skills": ["Communication", "Leadership"],
          "fit_score": 90.0,
          "seniority": "senior",
          "company_rating": 3.8,
          "company_type": "mid-size",
          "salary_min": 70000.0,
          "salary_max": 100000.0,
          "salary_currency": "EUR"
        }
        """),
        "Full stack role",
        "senior",
        List.of("Communication", "Leadership"),
        List.of("Java", "Spring", "React", "AWS"),
        90.0, 3.8, "mid-size", 70000.0, 100000.0, "EUR"),
      arguments(named("only technical skills",
        """
        {
          "summary": "Backend role",
          "technical_skills": ["Go", "Postgres"],
          "soft_skills": [],
          "fit_score": 70.0,
          "seniority": "mid",
          "company_rating": 2.9,
          "company_type": "startup",
          "salary_min": 60000.0,
          "salary_max": 85000.0,
          "salary_currency": "USD"
        }
        """),
        "Backend role",
        "mid",
        List.of(),
        List.of("Go", "Postgres"),
        70.0, 2.9, "startup", 60000.0, 85000.0, "USD"),
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
        "lead",
        List.of("Communication", "Empathy"),
        List.of(),
        40.0, 0.0, "unknown", 0.0, 0.0, "USD"),
      arguments(named("JSON with prose prefix and suffix",
        """
        Based on the job description, here is my analysis:
        {
          "summary": "Java backend role",
          "technical_skills": ["Java", "Spring"],
          "soft_skills": ["Teamwork"],
          "fit_score": 85.0,
          "seniority": "senior",
          "company_rating": 4.2,
          "company_type": "enterprise",
          "salary_min": 90000.0,
          "salary_max": 130000.0,
          "salary_currency": "USD"
        }
        I hope this helps!
        """),
        "Java backend role",
        "senior",
        List.of("Teamwork"),
        List.of("Java", "Spring"),
        85.0, 4.2, "enterprise", 90000.0, 130000.0, "USD"),
      arguments(named("JSON inside markdown code fences",
        """
        ```json
        {
          "summary": "Java backend role",
          "technical_skills": ["Java", "Spring"],
          "soft_skills": ["Teamwork"],
          "fit_score": 85.0,
          "seniority": "senior",
          "company_rating": 4.2,
          "company_type": "enterprise",
          "salary_min": 90000.0,
          "salary_max": 130000.0,
          "salary_currency": "USD"
        }
        ```
        """),
        "Java backend role",
        "senior",
        List.of("Teamwork"),
        List.of("Java", "Spring"),
        85.0, 4.2, "enterprise", 90000.0, 130000.0, "USD")
    );
  }

  private static Stream<Arguments> invalidResponses() {
    return Stream.of(
      arguments(named("null response", null), "Analysis pending"),
      arguments(named("empty response", ""), "Analysis pending"),
      arguments(named("malformed JSON", "not valid json at all"), "Parse error")
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("validResponses")
  void shouldParseResponse(final String response,
                           final String expectedSummary,
                           final String expectedSeniority,
                           final List<String> expectedSoftSkills,
                           final List<String> expectedTechnicalSkills,
                           final double expectedFitScore,
                           final double expectedCompanyRating,
                           final String expectedCompanyType,
                           final double expectedSalaryMin,
                           final double expectedSalaryMax,
                           final String expectedSalaryCurrency) {
    assertThat(parser.parse(response))
      .extracting(JobAnalysis::summary, JobAnalysis::seniority,
        JobAnalysis::softSkills, JobAnalysis::technicalSkills, JobAnalysis::fitScore,
        JobAnalysis::companyRating, JobAnalysis::companyType,
        JobAnalysis::salaryMin, JobAnalysis::salaryMax, JobAnalysis::salaryCurrency)
      .containsExactly(expectedSummary, expectedSeniority,
        expectedSoftSkills, expectedTechnicalSkills, expectedFitScore,
        expectedCompanyRating, expectedCompanyType,
        expectedSalaryMin, expectedSalaryMax, expectedSalaryCurrency);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidResponses")
  void shouldReturnFallbackForInvalidResponse(final String response, final String expectedSummaryPrefix) {
    assertThat(parser.parse(response)).matches(r ->
      r.summary().startsWith(expectedSummaryPrefix)
        && r.seniority().isEmpty()
        && r.softSkills().isEmpty()
        && r.technicalSkills().isEmpty()
        && r.fitScore() == FALLBACK_FIT_SCORE
        && r.companyRating() == FALLBACK_COMPANY_RATING
        && r.companyType().equals("unknown")
        && r.salaryMin() == FALLBACK_SALARY_MIN
        && r.salaryMax() == FALLBACK_SALARY_MAX
        && r.salaryCurrency().equals("USD"));
  }
}
