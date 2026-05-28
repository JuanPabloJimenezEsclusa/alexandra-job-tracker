package dev.jpje.jobtracker.api.resolver;

import java.util.Objects;
import java.util.UUID;

import dev.jpje.jobtracker.api.dto.JobApplicationResponse;
import dev.jpje.jobtracker.domain.port.in.TrackJobApplicationPort;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.Notes;
import dev.jpje.jobtracker.domain.vo.RoleName;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ApplicationMutationResolver {
  private final TrackJobApplicationPort useCase;

  public ApplicationMutationResolver(final TrackJobApplicationPort useCase) {
    this.useCase = useCase;
  }

  @MutationMapping
  public JobApplicationResponse createApplication(@ContextValue(required = false) @Nullable final UserId userId,
                                                   @Argument final String company,
                                                   @Argument final String role,
                                                   @Argument final Source source,
                                                   @Argument @Nullable final String postingUrl,
                                                   @Argument @Nullable final String notes) {
    Objects.requireNonNull(userId, "Authentication required");
    return JobApplicationResponse.from(useCase.create(userId,
      CompanyName.of(StringSanitizer.sanitize(company)),
      RoleName.of(StringSanitizer.sanitize(role)),
      source,
      postingUrl != null ? Url.of(StringSanitizer.sanitize(postingUrl)) : null,
      notes != null ? Notes.of(StringSanitizer.sanitize(notes)) : null));
  }

  @MutationMapping
  public JobApplicationResponse updateApplicationStatus(@Argument final UUID id,
                                                        @Argument final ApplicationStatus status,
                                                        @Argument @Nullable final String notes) {
    return JobApplicationResponse.from(useCase.updateStatus(id, status,
      notes != null ? Notes.of(StringSanitizer.sanitize(notes)) : null));
  }

  @MutationMapping
  public boolean deleteApplication(@Argument final UUID id) {
    useCase.delete(id);
    return true;
  }
}
