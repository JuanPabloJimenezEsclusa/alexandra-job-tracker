package com.jobtracker.api.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.jobtracker.domain.model.JobPosting;
import com.jobtracker.domain.port.in.ListJobPostingsUseCase;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobPostingQueryResolverTest {

  @InjectMocks
  private JobPostingQueryResolver resolver;

  @Mock
  private ListJobPostingsUseCase useCase;

  @Test
  void shouldListJobPostings() {
    final var userId = new UserId(UUID.randomUUID());
    final var posting = new JobPosting(UUID.randomUUID(), userId, "url", Source.LINKEDIN, "title", "company", "desc", Instant.now());
    final var expected = List.of(posting);

    when(useCase.listJobPostings(userId, null)).thenReturn(expected);

    assertThat(resolver.jobPostings(userId, null)).isEqualTo(expected);

    verify(useCase).listJobPostings(userId, null);
    verifyNoMoreInteractions(useCase);
  }
}
