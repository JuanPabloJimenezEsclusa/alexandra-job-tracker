package com.jobtracker.api.resolver;

import java.util.List;
import java.util.UUID;

import com.jobtracker.application.service.TrackJobApplicationUseCaseImpl;
import com.jobtracker.domain.model.JobApplication;
import com.jobtracker.domain.port.out.LoadJobApplicationPort;
import com.jobtracker.domain.port.out.SaveJobApplicationPort;
import com.jobtracker.domain.vo.ApplicationStatus;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ApplicationResolver {
  private final TrackJobApplicationUseCaseImpl useCase;

  public ApplicationResolver(final SaveJobApplicationPort savePort,
                             final LoadJobApplicationPort loadPort) {
    this.useCase = new TrackJobApplicationUseCaseImpl(savePort, loadPort);
  }

  @QueryMapping
  public List<JobApplication> applications(@ContextValue final UserId userId,
                                           @Argument final ApplicationStatus status,
                                           @Argument final Source source) {
    return useCase.list(userId, status, source);
  }

  @MutationMapping
  public JobApplication createApplication(@ContextValue final UserId userId,
                                          @Argument final String company,
                                          @Argument final String role,
                                          @Argument final Source source,
                                          @Argument final String postingUrl,
                                          @Argument final String notes) {
    return useCase.create(userId, company, role, source, postingUrl, notes);
  }

  @MutationMapping
  public JobApplication updateApplicationStatus(@Argument final UUID id,
                                                @Argument final ApplicationStatus status,
                                                @Argument final String notes) {
    return useCase.updateStatus(id, status, notes);
  }

  @MutationMapping
  public boolean deleteApplication(@Argument final UUID id) {
    useCase.delete(id);
    return true;
  }
}
