package dev.jpje.jobtracker.api.security;

import dev.jpje.jobtracker.domain.exception.ForbiddenException;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class Authz {
  private static final String ROLE_ADMIN = "ROLE_ADMIN";

  public boolean requireUser(@Nullable final Authentication authentication) {
    if (!isAuthenticated(authentication)) {
      throw new ForbiddenException("Authentication required");
    }
    return true;
  }

  public boolean requireAdmin(@Nullable final Authentication authentication) {
    requireUser(authentication);
    if (authentication == null || !hasAdminRole(authentication)) {
      throw new ForbiddenException("Admin access required");
    }
    return true;
  }

  private static boolean isAuthenticated(@Nullable final Authentication authentication) {
    return authentication != null
      && authentication.isAuthenticated()
      && !(authentication instanceof AnonymousAuthenticationToken);
  }

  private static boolean hasAdminRole(final Authentication authentication) {
    return authentication.getAuthorities().stream()
      .anyMatch(authority -> ROLE_ADMIN.equals(authority.getAuthority()));
  }
}
