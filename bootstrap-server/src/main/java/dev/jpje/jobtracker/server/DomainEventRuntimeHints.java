package dev.jpje.jobtracker.server;

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
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class DomainEventRuntimeHints implements RuntimeHintsRegistrar {

  @Override
  public void registerHints(final RuntimeHints hints, @Nullable final ClassLoader classLoader) {
    registerForBinding(hints
    );
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
      ApplicationStatus.class}) {
      hints.reflection().registerType(type, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS,
        MemberCategory.INVOKE_DECLARED_METHODS, MemberCategory.ACCESS_PUBLIC_FIELDS,
        MemberCategory.ACCESS_DECLARED_FIELDS);
    }
  }
}
