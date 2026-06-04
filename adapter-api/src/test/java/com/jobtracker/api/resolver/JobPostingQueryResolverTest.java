package com.jobtracker.api.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import com.jobtracker.domain.model.JobPosting;
import com.jobtracker.domain.port.in.ListJobPostingsUseCase;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import org.instancio.Instancio;
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
    final var posting = Instancio.of(JobPosting.class)
      .set(field(JobPosting::userId), userId)
      .set(field(JobPosting::source), Source.LINKEDIN)
      .create();
    final var expected = List.of(posting);

    when(useCase.listJobPostings(userId, null)).thenReturn(expected);

    assertThat(resolver.jobPostings(userId, null)).isEqualTo(expected);

    verify(useCase).listJobPostings(userId, null);
    verifyNoMoreInteractions(useCase);
  }
}
