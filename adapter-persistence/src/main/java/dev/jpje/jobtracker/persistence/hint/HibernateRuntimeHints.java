package dev.jpje.jobtracker.persistence.hint;

import org.hibernate.persister.state.internal.SoftDeleteStateManagement;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class HibernateRuntimeHints implements RuntimeHintsRegistrar {

  @Override
  public void registerHints(final RuntimeHints hints, @Nullable final ClassLoader classLoader) {
    registerForBinding(hints);
  }

  private static void registerForBinding(final RuntimeHints hints) {
    for (final Class<?> type : new Class<?>[]{
      SoftDeleteStateManagement.class}) {
      hints.reflection().registerType(type, MemberCategory.values());
    }
  }
}
