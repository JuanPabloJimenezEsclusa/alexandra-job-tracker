package dev.jpje.jobtracker.application.usecase;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobAnalysisRecord;
import dev.jpje.jobtracker.domain.port.in.ManageJobAnalysisPort;
import dev.jpje.jobtracker.domain.port.out.LoadJobAnalysisPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobAnalysisPort;
import dev.jpje.jobtracker.domain.vo.UserId;

public class ManageJobAnalysisUseCase implements ManageJobAnalysisPort {
  private final LoadJobAnalysisPort loadPort;
  private final SaveJobAnalysisPort savePort;

  public ManageJobAnalysisUseCase(final LoadJobAnalysisPort loadPort, final SaveJobAnalysisPort savePort) {
    this.loadPort = loadPort;
    this.savePort = savePort;
  }

  @Override
  public Optional<JobAnalysisRecord> findByIdForUser(final UserId userId, final UUID id) {
    return loadPort.findByIdAndUser(id, userId);
  }

  @Override
  public List<JobAnalysisRecord> findByUserId(final UserId userId) {
    return loadPort.findByUserId(userId);
  }

  @Override
  public void delete(final UUID id) {
    savePort.delete(id);
  }
}
