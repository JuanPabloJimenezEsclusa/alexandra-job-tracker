package dev.jpje.jobtracker.auth.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import dev.jpje.jobtracker.auth.userdetails.AuthUserDetails;
import dev.jpje.jobtracker.domain.model.User;
import dev.jpje.jobtracker.domain.port.outbound.LoadUserPort;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.domain.vo.UserRole;
import dev.jpje.jobtracker.domain.vo.Username;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class DbUserDetailsServiceTest {

  @InjectMocks
  private DbUserDetailsService service;

  @Mock
  private LoadUserPort loadUserPort;

  @Test
  void shouldLoadExistingUser() {
    final var user = Instancio.of(User.class)
      .set(field(User::id), new UserId(UUID.randomUUID()))
      .set(field(User::username), Username.of("alice"))
      .set(field(User::passwordHash), "hash")
      .set(field(User::role), UserRole.ADMIN)
      .create();
    when(loadUserPort.findByUsername("alice")).thenReturn(Optional.of(user));

    final var details = service.loadUserByUsername("alice");

    assertThat(details).isInstanceOf(AuthUserDetails.class);
    assertThat(details.getUsername()).isEqualTo("alice");
    assertThat(details.getPassword()).isEqualTo("hash");
    assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
  }

  @Test
  void shouldRejectUnknownUser() {
    when(loadUserPort.findByUsername("nobody")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.loadUserByUsername("nobody"))
      .isInstanceOf(UsernameNotFoundException.class);
  }
}
