package dev.jpje.jobtracker.domain.port.in;

import java.util.UUID;

import dev.jpje.jobtracker.domain.vo.JobAnalysis;

public interface AnalyzeJobPostingPort {
  JobAnalysis analyze(UUID jobPostingId);
}
