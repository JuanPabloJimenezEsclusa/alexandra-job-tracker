package dev.jpje.jobtracker.api.resolver;

import java.util.List;

import dev.jpje.jobtracker.api.dto.JobApplicationResponse;
import dev.jpje.jobtracker.domain.port.inbound.TrackJobApplicationPort;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
public class ApplicationQueryResolver {
  private final TrackJobApplicationPort useCase;

  public ApplicationQueryResolver(final TrackJobApplicationPort useCase) {
    this.useCase = useCase;
  }

  @QueryMapping
  @PreAuthorize("@authz.requireUser(authentication)")
  public List<JobApplicationResponse> applications(@AuthenticationPrincipal final UserId userId,
                                                   @Argument @Nullable final ApplicationStatus status) {
    return useCase.list(userId, status).stream()
      .map(JobApplicationResponse::from)
      .toList();
  }
}
