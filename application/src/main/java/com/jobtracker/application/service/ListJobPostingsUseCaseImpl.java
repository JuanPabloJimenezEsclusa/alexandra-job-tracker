package com.jobtracker.application.service;

import java.util.List;

import com.jobtracker.domain.model.JobPosting;
import com.jobtracker.domain.port.in.ListJobPostingsUseCase;
import com.jobtracker.domain.port.out.LoadJobPostingPort;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;

/**
 * Implementation of ListJobPostingsUseCase using LoadJobPostingPort.
 */
public class ListJobPostingsUseCaseImpl implements ListJobPostingsUseCase {
  private final LoadJobPostingPort loadJobPostingPort;

  /**
   * Constructor.
   */
  public ListJobPostingsUseCaseImpl(final LoadJobPostingPort loadJobPostingPort) {
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
