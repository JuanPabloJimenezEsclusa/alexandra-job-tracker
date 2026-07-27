package dev.jpje.jobtracker.application.usecase;

import java.util.List;

import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.port.in.ListJobPostingsPort;
import dev.jpje.jobtracker.domain.port.out.LoadJobPostingPort;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;

public class ListJobPostingsUseCase implements ListJobPostingsPort {
  private final LoadJobPostingPort loadJobPostingPort;

  public ListJobPostingsUseCase(final LoadJobPostingPort loadJobPostingPort) {
    this.loadJobPostingPort = loadJobPostingPort;
  }

  @Override
  public List<JobPosting> listJobPostings(final UserId userId, @Nullable final Source source) {
    var postings = loadJobPostingPort.findByUserId(userId);
    if (source != null) {
      postings = postings.stream()
        .filter(p -> p.source() == source)
        .toList();
    }
    return postings;
  }
}
