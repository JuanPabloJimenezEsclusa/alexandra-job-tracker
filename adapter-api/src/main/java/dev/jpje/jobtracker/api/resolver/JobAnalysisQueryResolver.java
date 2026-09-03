package dev.jpje.jobtracker.api.resolver;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import dev.jpje.jobtracker.api.dto.JobAnalysisResponse;
import dev.jpje.jobtracker.domain.exception.ResourceNotFoundException;
import dev.jpje.jobtracker.domain.port.in.ManageJobAnalysisPort;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class JobAnalysisQueryResolver {
  private final ManageJobAnalysisPort useCase;

  public JobAnalysisQueryResolver(final ManageJobAnalysisPort useCase) {
    this.useCase = useCase;
  }

  @QueryMapping
  public List<JobAnalysisResponse> analyses(@ContextValue(required = false) @Nullable final UserId userId) {
    Objects.requireNonNull(userId, "Authentication required");
    return useCase.findByUserId(userId).stream()
      .map(JobAnalysisResponse::from)
      .toList();
  }

  @QueryMapping
  public JobAnalysisResponse analysis(@ContextValue(required = false) @Nullable final UserId userId,
                                      @Argument final UUID id) {
    Objects.requireNonNull(userId, "Authentication required");
    return useCase.findByIdForUser(userId, id)
      .map(JobAnalysisResponse::from)
      .orElseThrow(() -> new ResourceNotFoundException("Analysis not found"));
  }
}
