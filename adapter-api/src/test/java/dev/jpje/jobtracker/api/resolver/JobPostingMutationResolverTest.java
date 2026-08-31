package dev.jpje.jobtracker.api.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;

import dev.jpje.jobtracker.api.dto.JobAnalysisResponse;
import dev.jpje.jobtracker.api.dto.JobPostingResponse;
import dev.jpje.jobtracker.domain.exception.ForbiddenException;
import dev.jpje.jobtracker.domain.model.JobAnalysisRecord;
import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.port.in.AnalyzeJobPostingPort;
import dev.jpje.jobtracker.domain.port.in.ManageJobAnalysisPort;
import dev.jpje.jobtracker.domain.port.in.SubmitJobPostingPort;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobAnalysis;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.domain.vo.UserRole;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobPostingMutationResolverTest {

  @InjectMocks
  private JobPostingMutationResolver resolver;

  @Mock
  private SubmitJobPostingPort submitUseCase;

  @Mock
  private AnalyzeJobPostingPort analyzeUseCase;

  @Mock
  private ManageJobAnalysisPort manageAnalysisUseCase;

  private static JobAnalysis validAnalysis() {
    return Instancio.of(JobAnalysis.class)
      .set(field(JobAnalysis::fitScore), 85.0)
      .set(field(JobAnalysis::companyRating), 4.2)
      .set(field(JobAnalysis::companyType), "enterprise")
      .set(field(JobAnalysis::salaryMin), 90000.0)
      .set(field(JobAnalysis::salaryMax), 130000.0)
      .set(field(JobAnalysis::salaryCurrency), "USD")
      .create();
  }

  @Test
  void shouldResolveAnalyze() {
    // Given
    final var userId = UserId.generate();
    final var jobPostingId = UUID.randomUUID();
    final var jobAnalysisRecord = Instancio.of(JobAnalysisRecord.class)
      .set(field(JobAnalysisRecord::jobPostingId), jobPostingId)
      .set(field(JobAnalysisRecord::userId), userId)
      .set(field(JobAnalysisRecord::analysis), validAnalysis())
      .create();
    when(analyzeUseCase.analyze(userId, jobPostingId)).thenReturn(jobAnalysisRecord);

    // When, then
    assertThat(resolver.analyzeJobPosting(userId, jobPostingId))
      .as("resolved analysis should match the recorded analysis")
      .isEqualTo(JobAnalysisResponse.from(jobAnalysisRecord));

    verify(analyzeUseCase, description("analysis should be requested once")).analyze(userId, jobPostingId);
    verifyNoMoreInteractions(analyzeUseCase);
    verifyNoInteractions(submitUseCase, manageAnalysisUseCase);
  }

  @Test
  void shouldThrowWhenAnalyzeFails() {
    // Given
    final var userId = UserId.generate();
    final var jobPostingId = UUID.randomUUID();
    final var errorMessage = "Job posting not found";
    when(analyzeUseCase.analyze(userId, jobPostingId)).thenThrow(new IllegalArgumentException(errorMessage));

    // When, then
    assertThatThrownBy(() -> resolver.analyzeJobPosting(userId, jobPostingId))
      .as("analyze should propagate the failure")
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage(errorMessage);

    verifyNoInteractions(submitUseCase, manageAnalysisUseCase);
  }

  @Test
  void shouldDeleteAnalysisAsAdmin() {
    final var id = UUID.randomUUID();
    final var userId = new UserId(UUID.randomUUID());

    assertThat(resolver.deleteAnalysis(userId, UserRole.ADMIN, id)).as("deletion should succeed").isTrue();

    verify(manageAnalysisUseCase, description("analysis deletion should be delegated")).delete(id);
    verifyNoInteractions(submitUseCase, analyzeUseCase);
  }

  @Test
  void shouldRejectDeleteAnalysisWithoutAuth() {
    final var id = UUID.randomUUID();

    assertThatThrownBy(() -> resolver.deleteAnalysis(null, UserRole.ADMIN, id))
      .as("delete without auth should fail")
      .isInstanceOf(NullPointerException.class)
      .hasMessage("Authentication required");
    verifyNoInteractions(submitUseCase, analyzeUseCase, manageAnalysisUseCase);
  }

  @Test
  void shouldRejectDeleteAnalysisForNonAdmin() {
    final var id = UUID.randomUUID();
    final var userId = new UserId(UUID.randomUUID());

    assertThatThrownBy(() -> resolver.deleteAnalysis(userId, UserRole.USER, id))
      .as("delete as non-admin should fail")
      .isInstanceOf(ForbiddenException.class)
      .hasMessage("Admin access required");
    verifyNoInteractions(submitUseCase, analyzeUseCase, manageAnalysisUseCase);
  }

  @Test
  void shouldSubmitJobPosting() {
    final var userId = new UserId(UUID.randomUUID());
    final var posted = Instancio.of(JobPosting.class)
      .set(field(JobPosting::userId), userId)
      .set(field(JobPosting::source), Source.LINKEDIN)
      .set(field(JobPosting::url), Url.of("https://example.com/job"))
      .set(field(JobPosting::title), JobTitle.of("title"))
      .set(field(JobPosting::company), CompanyName.of("company"))
      .set(field(JobPosting::description), "desc")
      .create();
    final var input = new JobPostingMutationResolver.JobPostingInput("https://example.com/job", "title", "company", "desc", Source.LINKEDIN);

    when(submitUseCase.submit(userId, Url.of("https://example.com/job"), JobTitle.of("title"),
      CompanyName.of("company"), "desc", Source.LINKEDIN)).thenReturn(posted);

    final var result = resolver.submitJobPosting(userId, input);
    assertThat(result)
      .as("submitted posting should reflect the input")
      .extracting(JobPostingResponse::source, JobPostingResponse::title, JobPostingResponse::company)
      .containsExactly(Source.LINKEDIN, "title", "company");

    verify(submitUseCase, description("posting should be submitted once")).submit(userId, Url.of("https://example.com/job"), JobTitle.of("title"),
      CompanyName.of("company"), "desc", Source.LINKEDIN);
    verifyNoMoreInteractions(submitUseCase);
    verifyNoInteractions(analyzeUseCase, manageAnalysisUseCase);
  }
}
