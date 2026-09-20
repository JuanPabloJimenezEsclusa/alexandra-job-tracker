package dev.jpje.jobtracker.auth.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import dev.jpje.jobtracker.auth.userdetails.AuthUserDetails;
import dev.jpje.jobtracker.domain.model.User;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.domain.vo.UserRole;
import dev.jpje.jobtracker.domain.vo.Username;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class AuthenticationManagerAdapterTest {

  private static final String PASSWORD = "secret";
  private static final String JBCRYPT_SEED_PASSWORD = "password123";
  private static final String JBCRYPT_SEED_HASH =
    "$2a$10$jWl7BKHc.pVGpWTOHrIKqer5bzhR.aPra/S/R6j55GRa07Pqw0zJG";
  private static final User USER = new User(UserId.generate(), Username.of("alice"),
    new BCryptPasswordEncoder(10).encode(PASSWORD), UserRole.USER, Instant.EPOCH);
  private static final User LEGACY_USER = new User(UserId.generate(), Username.of("legacy"),
    JBCRYPT_SEED_HASH, UserRole.USER, Instant.EPOCH);

  private final AuthenticationManagerAdapter adapter = new AuthenticationManagerAdapter(
    new ProviderManager(providerFor(USER)));

  @Test
  void shouldAuthenticateValidCredentials() {
    assertThat(adapter.authenticate("alice", PASSWORD)).isEqualTo(USER);
  }

  @Test
  void shouldRejectWrongPassword() {
    assertThatThrownBy(() -> adapter.authenticate("alice", "wrong"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Invalid credentials");
  }

  @Test
  void shouldRejectUnknownUser() {
    assertThatThrownBy(() -> adapter.authenticate("nobody", PASSWORD))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Invalid credentials");
  }

  @Test
  void shouldAuthenticateExistingJbcryptSeedHash() {
    final var legacyAdapter = new AuthenticationManagerAdapter(
      new ProviderManager(providerFor(LEGACY_USER)));

    assertThat(legacyAdapter.authenticate("legacy", JBCRYPT_SEED_PASSWORD))
      .as("existing jbcrypt $2a$ hashes must keep authenticating")
      .isEqualTo(LEGACY_USER);
  }

  private static DaoAuthenticationProvider providerFor(final User user) {
    final var provider = new DaoAuthenticationProvider(username -> {
      if (user.username().value().equals(username)) {
        return new AuthUserDetails(user);
      }
      throw new UsernameNotFoundException("User not found: " + username);
    });
    provider.setPasswordEncoder(new BCryptPasswordEncoder(10));
    return provider;
  }
}
