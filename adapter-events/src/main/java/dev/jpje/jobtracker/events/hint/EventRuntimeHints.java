package dev.jpje.jobtracker.events.hint;

import dev.jpje.jobtracker.domain.event.JobApplicationStatusChanged;
import dev.jpje.jobtracker.domain.event.JobPostingCreated;
import dev.jpje.jobtracker.domain.event.UserRegistered;
import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.domain.vo.Username;
import dev.jpje.jobtracker.events.api.SqsEventEnvelope;
import dev.jpje.jobtracker.events.api.SqsEventRecord;
import io.awspring.cloud.sns.core.SnsTemplate;
import io.awspring.cloud.sns.core.TopicMessageChannel;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import software.amazon.awssdk.services.sns.SnsClient;

public class EventRuntimeHints implements RuntimeHintsRegistrar {

  @Override
  public void registerHints(final RuntimeHints hints, @Nullable final ClassLoader classLoader) {
    registerForBinding(hints);
  }

  private static void registerForBinding(final RuntimeHints hints) {
    for (final Class<?> type : new Class<?>[]{
      JobPostingCreated.class,
      JobPosting.class,
      UserRegistered.class,
      JobApplicationStatusChanged.class,
      UserId.class,
      Username.class,
      Url.class,
      JobTitle.class,
      CompanyName.class,
      Source.class,
      ApplicationStatus.class,
      // AWS client classes
      SnsTemplate.class,
      TopicMessageChannel.class,
      SnsClient.class,
      SqsEventEnvelope.class,
      SqsEventRecord.class}) {
      hints.reflection().registerType(type, MemberCategory.values());
    }
  }
}
