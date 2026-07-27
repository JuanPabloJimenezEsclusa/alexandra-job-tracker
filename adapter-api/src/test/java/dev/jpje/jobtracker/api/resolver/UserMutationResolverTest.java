package dev.jpje.jobtracker.api.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import dev.jpje.jobtracker.domain.model.AuthPayload;
import dev.jpje.jobtracker.domain.model.User;
import dev.jpje.jobtracker.domain.port.in.AuthenticationPort;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserMutationResolverTest {

  @InjectMocks
  private UserMutationResolver resolver;

  @Mock
  private AuthenticationPort authUseCase;

  @Test
  void shouldRegister() {
    final var user = Instancio.of(User.class)
      .set(field(User::username), "alice")
      .set(field(User::passwordHash), "hash")
      .create();
    final var payload = new AuthPayload("token", user);

    when(authUseCase.register("alice", "secret")).thenReturn(payload);

    final var result = resolver.register("alice", "secret");
    assertThat(result.token()).isEqualTo("token");
    assertThat(result.user().username()).isEqualTo("alice");

    verify(authUseCase).register("alice", "secret");
    verifyNoMoreInteractions(authUseCase);
  }

  @Test
  void shouldLogin() {
    final var user = Instancio.of(User.class)
      .set(field(User::username), "alice")
      .set(field(User::passwordHash), "hash")
      .create();
    final var payload = new AuthPayload("token", user);

    when(authUseCase.login("alice", "secret")).thenReturn(payload);

    final var result = resolver.login("alice", "secret");
    assertThat(result.token()).isEqualTo("token");
    assertThat(result.user().username()).isEqualTo("alice");

    verify(authUseCase).login("alice", "secret");
    verifyNoMoreInteractions(authUseCase);
  }

  @Test
  void shouldLogout() {
    assertThat(resolver.logout()).isTrue();
  }
}
