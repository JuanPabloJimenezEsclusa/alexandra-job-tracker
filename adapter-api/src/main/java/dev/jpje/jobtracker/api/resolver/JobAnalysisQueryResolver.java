package dev.jpje.jobtracker.api.resolver;

import java.util.List;
import java.util.UUID;

import dev.jpje.jobtracker.api.dto.JobAnalysisResponse;
import dev.jpje.jobtracker.domain.exception.ResourceNotFoundException;
import dev.jpje.jobtracker.domain.port.inbound.ManageJobAnalysisPort;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
public class JobAnalysisQueryResolver {
  private final ManageJobAnalysisPort useCase;

  public JobAnalysisQueryResolver(final ManageJobAnalysisPort useCase) {
    this.useCase = useCase;
  }

  @QueryMapping
  @PreAuthorize("@authz.requireUser(authentication)")
  public List<JobAnalysisResponse> analyses(@AuthenticationPrincipal final UserId userId) {
    return useCase.findByUserId(userId).stream()
      .map(JobAnalysisResponse::from)
      .toList();
  }

  @QueryMapping
  @PreAuthorize("@authz.requireUser(authentication)")
  public JobAnalysisResponse analysis(@AuthenticationPrincipal final UserId userId,
                                      @Argument final UUID id) {
    return useCase.findByIdForUser(userId, id)
      .map(JobAnalysisResponse::from)
      .orElseThrow(() -> new ResourceNotFoundException("Analysis not found"));
  }
}
