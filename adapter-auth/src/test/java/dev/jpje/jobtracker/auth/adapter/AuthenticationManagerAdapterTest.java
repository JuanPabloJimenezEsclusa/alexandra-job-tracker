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
  private static final User USER = new User(UserId.generate(), Username.of("alice"),
    new BCryptPasswordEncoder(10).encode(PASSWORD), UserRole.USER, Instant.EPOCH);

  private final AuthenticationManagerAdapter adapter = new AuthenticationManagerAdapter(
    new ProviderManager(provider()));

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

  private static DaoAuthenticationProvider provider() {
    final var provider = new DaoAuthenticationProvider(username -> {
      if (USER.username().value().equals(username)) {
        return new AuthUserDetails(USER);
      }
      throw new UsernameNotFoundException("User not found: " + username);
    });
    provider.setPasswordEncoder(new BCryptPasswordEncoder(10));
    return provider;
  }
}
