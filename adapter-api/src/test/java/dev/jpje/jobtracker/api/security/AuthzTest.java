package dev.jpje.jobtracker.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;

import dev.jpje.jobtracker.domain.exception.ForbiddenException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class AuthzTest {

  private static final String AUTHENTICATION_REQUIRED = "Authentication required";

  private final Authz authz = new Authz();

  @Test
  void shouldAllowAuthenticatedUser() {
    assertThat(authz.requireUser(authentication("ROLE_USER"))).isTrue();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("unauthenticatedRequests")
  void shouldRejectUnauthenticatedUser(final Authentication authentication) {
    assertThatThrownBy(() -> authz.requireUser(authentication))
      .isInstanceOf(ForbiddenException.class)
      .hasMessage(AUTHENTICATION_REQUIRED);
  }

  @Test
  void shouldAllowAdmin() {
    assertThat(authz.requireAdmin(authentication("ROLE_ADMIN"))).isTrue();
  }

  @Test
  void shouldRejectNonAdmin() {
    final var user = authentication("ROLE_USER");
    assertThatThrownBy(() -> authz.requireAdmin(user))
      .isInstanceOf(ForbiddenException.class)
      .hasMessage("Admin access required");
  }

  @Test
  void shouldRejectAdminWhenUnauthenticated() {
    assertThatThrownBy(() -> authz.requireAdmin(null))
      .isInstanceOf(ForbiddenException.class)
      .hasMessage(AUTHENTICATION_REQUIRED);
  }

  private static Stream<Arguments> unauthenticatedRequests() {
    return Stream.of(
      arguments(named("anonymous authentication", anonymous())),
      arguments(named("missing authentication", (Authentication) null))
    );
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
