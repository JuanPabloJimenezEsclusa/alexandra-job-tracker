package dev.jpje.jobtracker.domain.port.in;

import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobAnalysis;

public interface AnalyzeJobPostingPort {
  JobAnalysis analyze(UUID jobPostingId);
}
