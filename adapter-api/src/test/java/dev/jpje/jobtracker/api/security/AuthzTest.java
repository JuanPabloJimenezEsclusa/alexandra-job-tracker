package dev.jpje.jobtracker.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import dev.jpje.jobtracker.domain.exception.ForbiddenException;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class AuthzTest {

  private final Authz authz = new Authz();

  @Test
  void shouldAllowAuthenticatedUser() {
    assertThat(authz.requireUser(authentication("ROLE_USER"))).isTrue();
  }

  @Test
  void shouldRejectAnonymousUser() {
    assertThatThrownBy(() -> authz.requireUser(anonymous()))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Authentication required");
  }

  @Test
  void shouldRejectMissingAuthentication() {
    assertThatThrownBy(() -> authz.requireUser(null))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Authentication required");
  }

  @Test
  void shouldAllowAdmin() {
    assertThat(authz.requireAdmin(authentication("ROLE_ADMIN"))).isTrue();
  }

  @Test
  void shouldRejectNonAdmin() {
    assertThatThrownBy(() -> authz.requireAdmin(authentication("ROLE_USER")))
      .isInstanceOf(ForbiddenException.class)
      .hasMessage("Admin access required");
  }

  @Test
  void shouldRejectAdminWhenUnauthenticated() {
    assertThatThrownBy(() -> authz.requireAdmin(null))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Authentication required");
  }

  private static Authentication authentication(final String role) {
    return new UsernamePasswordAuthenticationToken("alice", null,
      List.of(new SimpleGrantedAuthority(role)));
  }

  private static Authentication anonymous() {
    return new AnonymousAuthenticationToken("key", "anonymous",
      List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
  }
}
