package dev.jpje.jobtracker.api.resolver;

import java.util.List;
import java.util.Objects;

import dev.jpje.jobtracker.api.dto.JobPostingResponse;
import dev.jpje.jobtracker.domain.port.in.ListJobPostingsPort;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class JobPostingQueryResolver {
  private final ListJobPostingsPort useCase;

  public JobPostingQueryResolver(final ListJobPostingsPort useCase) {
    this.useCase = useCase;
  }

  @QueryMapping
  public List<JobPostingResponse> jobPostings(@ContextValue(required = false) @Nullable final UserId userId,
                                               @Argument @Nullable final Source source) {
    Objects.requireNonNull(userId, "Authentication required");
    return useCase.listJobPostings(userId, source).stream()
      .map(JobPostingResponse::from)
      .toList();
  }
}
