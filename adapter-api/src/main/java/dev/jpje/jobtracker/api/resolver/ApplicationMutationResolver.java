package dev.jpje.jobtracker.api.resolver;

import java.util.UUID;

import dev.jpje.jobtracker.api.dto.JobApplicationResponse;
import dev.jpje.jobtracker.domain.port.inbound.TrackJobApplicationPort;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.Notes;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
public class ApplicationMutationResolver {

  private final TrackJobApplicationPort useCase;

  public ApplicationMutationResolver(final TrackJobApplicationPort useCase) {
    this.useCase = useCase;
  }

  @MutationMapping
  @PreAuthorize("@authz.requireUser(authentication)")
  public JobApplicationResponse createApplication(@AuthenticationPrincipal final UserId userId,
                                                  @Argument final UUID jobPostingId,
                                                  @Argument @Nullable final String notes) {
    return JobApplicationResponse.from(useCase.create(userId, jobPostingId,
      notes != null ? Notes.of(StringSanitizer.sanitize(notes)) : null));
  }

  @MutationMapping
  @PreAuthorize("@authz.requireUser(authentication)")
  public JobApplicationResponse updateApplicationStatus(@AuthenticationPrincipal final UserId userId,
                                                        @Argument final UUID id,
                                                        @Argument final ApplicationStatus status,
                                                        @Argument @Nullable final String notes) {
    return JobApplicationResponse.from(useCase.updateStatus(userId, id, status,
      notes != null ? Notes.of(StringSanitizer.sanitize(notes)) : null));
  }

  @MutationMapping
  @PreAuthorize("@authz.requireUser(authentication)")
  public boolean deleteApplication(@AuthenticationPrincipal final UserId userId,
                                   @Argument final UUID id) {
    useCase.delete(userId, id);
    return true;
  }
}
