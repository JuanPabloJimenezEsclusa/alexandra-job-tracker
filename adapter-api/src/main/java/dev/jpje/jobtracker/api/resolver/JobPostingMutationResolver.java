package dev.jpje.jobtracker.api.resolver;

import java.util.UUID;

import dev.jpje.jobtracker.api.dto.JobAnalysisResponse;
import dev.jpje.jobtracker.api.dto.JobPostingResponse;
import dev.jpje.jobtracker.domain.port.inbound.AnalyzeJobPostingPort;
import dev.jpje.jobtracker.domain.port.inbound.ManageJobAnalysisPort;
import dev.jpje.jobtracker.domain.port.inbound.SubmitJobPostingPort;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
public class JobPostingMutationResolver {
  private final SubmitJobPostingPort submitUseCase;
  private final AnalyzeJobPostingPort analyzeUseCase;
  private final ManageJobAnalysisPort manageAnalysisUseCase;

  public JobPostingMutationResolver(final SubmitJobPostingPort submitUseCase,
                                     final AnalyzeJobPostingPort analyzeUseCase,
                                     final ManageJobAnalysisPort manageAnalysisUseCase) {
    this.submitUseCase = submitUseCase;
    this.analyzeUseCase = analyzeUseCase;
    this.manageAnalysisUseCase = manageAnalysisUseCase;
  }

  @MutationMapping
  @PreAuthorize("@authz.requireUser(authentication)")
  public JobPostingResponse submitJobPosting(@AuthenticationPrincipal final UserId userId,
                                              @Argument("input") final JobPostingInput raw) {
    return JobPostingResponse.from(submitUseCase.submit(userId,
      Url.of(StringSanitizer.sanitize(raw.url())),
      JobTitle.of(StringSanitizer.sanitize(raw.title())),
      CompanyName.of(StringSanitizer.sanitize(raw.company())),
      StringSanitizer.sanitize(raw.description()),
      raw.source()));
  }

  @MutationMapping
  @PreAuthorize("@authz.requireUser(authentication)")
  public JobAnalysisResponse analyzeJobPosting(@AuthenticationPrincipal final UserId userId,
                                               @Argument final UUID jobPostingId) {
    return JobAnalysisResponse.from(analyzeUseCase.analyze(userId, jobPostingId));
  }

  @MutationMapping
  @PreAuthorize("@authz.requireAdmin(authentication)")
  public boolean deleteAnalysis(@Argument final UUID id) {
    manageAnalysisUseCase.delete(id);
    return true;
  }

  public record JobPostingInput(
    String url,
    String title,
    String company,
    String description,
    Source source) {
  }
}
