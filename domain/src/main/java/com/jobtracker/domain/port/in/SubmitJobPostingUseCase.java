package com.jobtracker.domain.port.in;

import com.jobtracker.domain.model.JobPosting;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;

/**
 * Use case for submitting a job posting.
 */
public interface SubmitJobPostingUseCase {
  /**
   * Submits a job posting.
   */
  JobPosting submit(UserId userId, String url, String title, String company,
                    @Nullable String description, Source source);
}
