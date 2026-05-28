package dev.jpje.jobtracker.api.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import dev.jpje.jobtracker.api.dto.JobPostingResponse;
import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.port.in.ListJobPostingsPort;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;
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
  private ListJobPostingsPort useCase;

  @Test
  void shouldListJobPostings() {
    final var userId = new UserId(UUID.randomUUID());
    final var posting = Instancio.of(JobPosting.class)
      .set(field(JobPosting::userId), userId)
      .set(field(JobPosting::source), Source.LINKEDIN)
      .set(field(JobPosting::url), Url.of("https://example.com/job"))
      .set(field(JobPosting::title), JobTitle.of("title"))
      .set(field(JobPosting::company), CompanyName.of("company"))
      .create();
    final var input = List.of(posting);

    when(useCase.listJobPostings(userId, null)).thenReturn(input);

    final var result = resolver.jobPostings(userId, null);
    assertThat(result)
      .singleElement()
      .extracting(JobPostingResponse::source)
      .isEqualTo(Source.LINKEDIN);

    verify(useCase).listJobPostings(userId, null);
    verifyNoMoreInteractions(useCase);
  }
}
