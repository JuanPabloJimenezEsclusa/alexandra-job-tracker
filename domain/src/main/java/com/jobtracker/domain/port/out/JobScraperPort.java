package com.jobtracker.domain.port.out;

import com.jobtracker.domain.vo.UserId;

public interface JobScraperPort {
  RawJobData scrape(UserId userId, String url);
}
