package dev.jpje.jobtracker.api.resolver;

import java.util.List;

import dev.jpje.jobtracker.api.dto.JobPostingResponse;
import dev.jpje.jobtracker.domain.port.inbound.ListJobPostingsPort;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
public class JobPostingQueryResolver {
  private final ListJobPostingsPort useCase;

  public JobPostingQueryResolver(final ListJobPostingsPort useCase) {
    this.useCase = useCase;
  }

  @QueryMapping
  @PreAuthorize("@authz.requireUser(authentication)")
  public List<JobPostingResponse> jobPostings(@AuthenticationPrincipal final UserId userId,
                                               @Argument @Nullable final Source source) {
    return useCase.listJobPostings(userId, source).stream()
      .map(JobPostingResponse::from)
      .toList();
  }
}
