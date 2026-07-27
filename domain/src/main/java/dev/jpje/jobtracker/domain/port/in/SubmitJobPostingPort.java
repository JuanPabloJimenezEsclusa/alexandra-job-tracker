package dev.jpje.jobtracker.domain.port.in;

import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;

public interface SubmitJobPostingPort {
  JobPosting submit(UserId userId, Url url, JobTitle title, CompanyName company,
                    String description, Source source);
}
