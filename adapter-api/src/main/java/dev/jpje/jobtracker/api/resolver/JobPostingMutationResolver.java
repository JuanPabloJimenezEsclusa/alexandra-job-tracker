package dev.jpje.jobtracker.api.resolver;

import java.util.Objects;
import java.util.UUID;

import dev.jpje.jobtracker.api.dto.JobPostingResponse;
import dev.jpje.jobtracker.domain.model.JobAnalysis;
import dev.jpje.jobtracker.domain.port.in.AnalyzeJobPostingPort;
import dev.jpje.jobtracker.domain.port.in.SubmitJobPostingPort;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class JobPostingMutationResolver {
  private final SubmitJobPostingPort submitUseCase;
  private final AnalyzeJobPostingPort analyzeUseCase;

  public JobPostingMutationResolver(final SubmitJobPostingPort submitUseCase,
                                     final AnalyzeJobPostingPort analyzeUseCase) {
    this.submitUseCase = submitUseCase;
    this.analyzeUseCase = analyzeUseCase;
  }

  @MutationMapping
  public JobPostingResponse submitJobPosting(@ContextValue(required = false) @Nullable final UserId userId,
                                              @Argument("input") final JobPostingInput raw) {
    Objects.requireNonNull(userId, "Authentication required");
    return JobPostingResponse.from(submitUseCase.submit(userId,
      StringSanitizer.sanitize(raw.url()),
      StringSanitizer.sanitize(raw.title()),
      StringSanitizer.sanitize(raw.company()),
      StringSanitizer.sanitize(raw.description()),
      raw.source()));
  }

  @MutationMapping
  public JobAnalysis analyzeJobPosting(@Argument final UUID jobPostingId) {
    return analyzeUseCase.analyze(jobPostingId);
  }

  public record JobPostingInput(
    String url,
    String title,
    String company,
    String description,
    Source source) {
  }
}
