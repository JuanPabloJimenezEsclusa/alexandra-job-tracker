package dev.jpje.jobtracker.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JobAnalysisTest {

  private static final double FULL_FIT_SCORE = 85.0;
  private static final double FULL_COMPANY_RATING = 4.2;
  private static final double FULL_SALARY_MIN = 90000.0;
  private static final double FULL_SALARY_MAX = 130000.0;

  private static final double EMPTY_FIT_SCORE = 0.0;
  private static final double EMPTY_COMPANY_RATING = 0.0;
  private static final double EMPTY_SALARY_MIN = 0.0;
  private static final double EMPTY_SALARY_MAX = 0.0;

  private static final double SINGLE_SKILL_FIT_SCORE = 100.0;
  private static final double SINGLE_SKILL_COMPANY_RATING = 3.0;
  private static final double SINGLE_SKILL_SALARY_MIN = 60000.0;
  private static final double SINGLE_SKILL_SALARY_MAX = 90000.0;

  private static final double MIN_FIT_SCORE = 0.0;
  private static final double MID_FIT_SCORE = 50.0;
  private static final double MAX_FIT_SCORE = 100.0;

  private static final double MIN_COMPANY_RATING = 0.0;
  private static final double MID_COMPANY_RATING = 2.5;
  private static final double MAX_COMPANY_RATING = 5.0;

  private static Stream<Arguments> validAnalysis() {
    return Stream.of(
      arguments(named("full analysis", "Great role"), "senior", List.of("Java", "Spring"),
        List.of("Teamwork"), FULL_FIT_SCORE, FULL_COMPANY_RATING, "enterprise", FULL_SALARY_MIN, FULL_SALARY_MAX, "USD"),
      arguments(named("empty analysis", ""), "", List.of(), List.of(), EMPTY_FIT_SCORE, EMPTY_COMPANY_RATING, "unknown",
        EMPTY_SALARY_MIN, EMPTY_SALARY_MAX, "USD"),
      arguments(named("single skill", "Needs experience"), "mid", List.of("Kubernetes"),
        List.of(), SINGLE_SKILL_FIT_SCORE, SINGLE_SKILL_COMPANY_RATING, "startup", SINGLE_SKILL_SALARY_MIN, SINGLE_SKILL_SALARY_MAX, "EUR")
    );
  }

  private static Stream<Arguments> fitScoreBoundaries() {
    return Stream.of(
      arguments(named("minimum", MIN_FIT_SCORE)),
      arguments(named("midpoint", MID_FIT_SCORE)),
      arguments(named("maximum", MAX_FIT_SCORE))
    );
  }

  private static Stream<Arguments> companyRatingBoundaries() {
    return Stream.of(
      arguments(named("minimum", MIN_COMPANY_RATING)),
      arguments(named("midpoint", MID_COMPANY_RATING)),
      arguments(named("maximum", MAX_COMPANY_RATING))
    );
  }

  private static Stream<Arguments> invalidInputs() {
    return Stream.of(
      arguments(named("null summary", null), "senior", List.of("Java"), List.of("Teamwork"), 50.0,
        3.0, "enterprise", 50000.0, 80000.0, "USD", "summary must not be null"),
      arguments(named("null seniority", "summary"), null, List.of("Java"), List.of("Teamwork"), 50.0,
        3.0, "enterprise", 50000.0, 80000.0, "USD", "seniority must not be null"),
      arguments(named("null soft skills", "summary"), "senior", null, List.of("Teamwork"), 50.0,
        3.0, "enterprise", 50000.0, 80000.0, "USD", "soft skills must not be null"),
      arguments(named("null technical skills", "summary"), "senior", List.of("Java"), null, 50.0,
        3.0, "enterprise", 50000.0, 80000.0, "USD", "technical skills must not be null"),
      arguments(named("null company type", "summary"), "senior", List.of(), List.of(), 50.0,
        3.0, null, 50000.0, 80000.0, "USD", "companyType must not be null"),
      arguments(named("null salary currency", "summary"), "senior", List.of(), List.of(), 50.0,
        3.0, "enterprise", 50000.0, 80000.0, null, "salaryCurrency must not be null"),
      arguments(named("fitScore too low", "summary"), "senior", List.of(), List.of(), -0.1,
        3.0, "enterprise", 50000.0, 80000.0, "USD", "fitScore must be between 0.0 and 100.0"),
      arguments(named("fitScore too high", "summary"), "senior", List.of(), List.of(), 100.1,
        3.0, "enterprise", 50000.0, 80000.0, "USD", "fitScore must be between 0.0 and 100.0"),
      arguments(named("companyRating too low", "summary"), "senior", List.of(), List.of(), 50.0,
        -0.1, "enterprise", 50000.0, 80000.0, "USD", "companyRating must be between 0.0 and 5.0"),
      arguments(named("companyRating too high", "summary"), "senior", List.of(), List.of(), 50.0,
        5.1, "enterprise", 50000.0, 80000.0, "USD", "companyRating must be between 0.0 and 5.0"),
      arguments(named("negative salary min", "summary"), "senior", List.of(), List.of(), 50.0,
        3.0, "enterprise", -1.0, 80000.0, "USD", "salary must not be negative"),
      arguments(named("salary max below min", "summary"), "senior", List.of(), List.of(), 50.0,
        3.0, "enterprise", 80000.0, 50000.0, "USD", "salaryMax must be greater than or equal to salaryMin")
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("validAnalysis")
  void shouldCreateJobAnalysis(final String summary, final String seniority,
                               final List<String> softSkills, final List<String> technicalSkills,
                               final double fitScore, final double companyRating, final String companyType,
                               final double salaryMin, final double salaryMax, final String salaryCurrency) {
    assertThat(new JobAnalysis(summary, seniority, softSkills, technicalSkills, fitScore,
      companyRating, companyType, salaryMin, salaryMax, salaryCurrency))
      .extracting(JobAnalysis::summary, JobAnalysis::seniority,
        JobAnalysis::softSkills, JobAnalysis::technicalSkills, JobAnalysis::fitScore,
        JobAnalysis::companyRating, JobAnalysis::companyType,
        JobAnalysis::salaryMin, JobAnalysis::salaryMax, JobAnalysis::salaryCurrency)
      .containsExactly(summary, seniority, softSkills, technicalSkills, fitScore,
        companyRating, companyType, salaryMin, salaryMax, salaryCurrency);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("fitScoreBoundaries")
  void shouldAcceptAllFitScoreValues(final double fitScore) {
    assertThat(new JobAnalysis("test", "senior", List.of(), List.of(), fitScore,
      3.0, "enterprise", 50000.0, 80000.0, "USD"))
      .extracting(JobAnalysis::fitScore)
      .isEqualTo(fitScore);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("companyRatingBoundaries")
  void shouldAcceptAllCompanyRatingValues(final double companyRating) {
    assertThat(new JobAnalysis("test", "senior", List.of(), List.of(), 50.0,
      companyRating, "enterprise", 50000.0, 80000.0, "USD"))
      .extracting(JobAnalysis::companyRating)
      .isEqualTo(companyRating);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidInputs")
  void shouldRejectInvalidInputs(final String summary, final String seniority,
                                 final List<String> softSkills, final List<String> technicalSkills,
                                 final double fitScore, final double companyRating, final String companyType,
                                 final double salaryMin, final double salaryMax, final String salaryCurrency,
                                 final String expectedMessage) {
    assertThatThrownBy(() -> new JobAnalysis(summary, seniority, softSkills, technicalSkills, fitScore,
      companyRating, companyType, salaryMin, salaryMax, salaryCurrency))
      .isInstanceOf(RuntimeException.class)
      .hasMessageContaining(expectedMessage);
  }
}
