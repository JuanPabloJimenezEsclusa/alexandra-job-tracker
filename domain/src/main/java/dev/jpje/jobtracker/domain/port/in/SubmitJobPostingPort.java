package dev.jpje.jobtracker.domain.port.in;

import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.UserId;

public interface SubmitJobPostingPort {
  JobPosting submit(UserId userId, String url, String title, String company, String description, Source source);
}
