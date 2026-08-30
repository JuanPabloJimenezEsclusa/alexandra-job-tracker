package dev.jpje.jobtracker.api.resolver;

import java.util.List;
import java.util.Objects;

import dev.jpje.jobtracker.api.dto.JobApplicationResponse;
import dev.jpje.jobtracker.domain.port.in.TrackJobApplicationPort;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ApplicationQueryResolver {
  private final TrackJobApplicationPort useCase;

  public ApplicationQueryResolver(final TrackJobApplicationPort useCase) {
    this.useCase = useCase;
  }

  @QueryMapping
  public List<JobApplicationResponse> applications(@ContextValue(required = false) @Nullable final UserId userId,
                                                    @Argument @Nullable final ApplicationStatus status) {
    Objects.requireNonNull(userId, "Authentication required");
    return useCase.list(userId, status).stream()
      .map(JobApplicationResponse::from)
      .toList();
  }
}
