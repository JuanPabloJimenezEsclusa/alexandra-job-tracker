package com.jobtracker.domain.port.in;

import java.util.List;

import com.jobtracker.domain.model.JobPosting;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;

/**
 * Use case for listing job postings.
 */
public interface ListJobPostingsUseCase {
  /**
   * Lists job postings for the given user, optionally filtered by source.
   */
  List<JobPosting> listJobPostings(UserId userId, @Nullable Source source);
}
