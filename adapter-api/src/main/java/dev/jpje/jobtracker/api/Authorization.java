package dev.jpje.jobtracker.api;

import dev.jpje.jobtracker.domain.exception.ForbiddenException;
import dev.jpje.jobtracker.domain.vo.UserRole;
import org.jspecify.annotations.Nullable;

public final class Authorization {

  private Authorization() {
  }

  public static void requireAdmin(@Nullable final UserRole role) {
    if (role != UserRole.ADMIN) {
      throw new ForbiddenException("Admin access required");
    }
  }
}
