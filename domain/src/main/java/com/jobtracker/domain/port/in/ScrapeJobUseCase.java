package com.jobtracker.domain.port.in;

import com.jobtracker.domain.model.JobPosting;
import com.jobtracker.domain.vo.UserId;

public interface ScrapeJobUseCase {
  JobPosting scrape(UserId userId, String url);
}
