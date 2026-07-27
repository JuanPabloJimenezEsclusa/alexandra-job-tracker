package dev.jpje.jobtracker.domain.port.in;

import java.util.List;

import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;

public interface ListJobPostingsPort {
  List<JobPosting> listJobPostings(UserId userId, @Nullable Source source);
}
