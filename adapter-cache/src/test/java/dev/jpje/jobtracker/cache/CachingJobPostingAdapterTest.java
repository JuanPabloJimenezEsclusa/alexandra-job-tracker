package dev.jpje.jobtracker.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.port.out.LoadJobPostingPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobPostingPort;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CachingJobPostingAdapterTest {

  @Mock
  private LoadJobPostingPort loadDelegate;

  @Mock
  private SaveJobPostingPort saveDelegate;

  @Spy
  private CaffeineCacheAdapter cache = new CaffeineCacheAdapter(100, Duration.ofMinutes(5));

  @InjectMocks
  private CachingJobPostingAdapter adapter;

  @Test
  void shouldReturnCachedPostingWhenPresent() {
    final var posting = jobPosting();
    primeByIdCache(posting.id(), posting);

    assertThat(adapter.findById(posting.id())).as("cached posting should be returned").hasValue(posting);
    verify(loadDelegate, description("load delegate should be hit only on cache miss")).findById(posting.id());
  }

  @Test
  void shouldLoadAndCachePostingOnMiss() {
    final var posting = jobPosting();
    when(loadDelegate.findById(posting.id())).thenReturn(Optional.of(posting));

    assertThat(adapter.findById(posting.id())).hasValue(posting);
  }

  @Test
  void shouldReturnCachedListWhenPresent() {
    final var userId = UserId.generate();
    final var posting = jobPosting(userId);
    primeListCache(userId, List.of(posting));

    assertThat(adapter.findByUserId(userId)).as("cached list should be returned").containsExactly(posting);
    verify(loadDelegate, description("load delegate should be hit only on cache miss")).findByUserId(userId);
  }

  @Test
  void shouldLoadAndCacheListOnMiss() {
    final var userId = UserId.generate();
    final var posting = jobPosting(userId);
    when(loadDelegate.findByUserId(userId)).thenReturn(List.of(posting));

    assertThat(adapter.findByUserId(userId)).isEqualTo(List.of(posting));
  }

  @Test
  void shouldSaveAndCachePosting() {
    final var posting = jobPosting();

    adapter.save(posting);

    assertThat(cache.asMap()).as("saved posting should be cached").containsKey("jobpost:" + posting.id());
    verify(saveDelegate, description("save should be delegated")).save(posting);
  }

  private void primeByIdCache(final UUID id, final JobPosting posting) {
    when(loadDelegate.findById(id)).thenReturn(Optional.of(posting));
    adapter.findById(id);
  }

  private void primeListCache(final UserId userId, final List<JobPosting> postings) {
    when(loadDelegate.findByUserId(userId)).thenReturn(postings);
    adapter.findByUserId(userId);
  }

  private static JobPosting jobPosting() {
    return jobPosting(UserId.generate());
  }

  private static JobPosting jobPosting(final UserId userId) {
    return Instancio.of(JobPosting.class)
      .set(field(JobPosting::userId), userId)
      .set(field(JobPosting::source), Source.LINKEDIN)
      .set(field(JobPosting::url), Url.of("https://example.com/job"))
      .set(field(JobPosting::title), JobTitle.of("Engineer"))
      .set(field(JobPosting::company), CompanyName.of("Acme"))
      .set(field(JobPosting::description), "desc")
      .create();
  }
}
