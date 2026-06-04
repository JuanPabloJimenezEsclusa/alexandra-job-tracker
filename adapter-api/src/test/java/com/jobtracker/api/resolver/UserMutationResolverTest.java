package com.jobtracker.api.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.jobtracker.domain.model.AuthPayload;
import com.jobtracker.domain.model.User;
import com.jobtracker.domain.port.in.AuthenticationUseCase;
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
  private AuthenticationUseCase authUseCase;

  @Test
  void shouldRegister() {
    final var user = Instancio.of(User.class)
      .set(field(User::username), "alice")
      .set(field(User::passwordHash), "hash")
      .create();
    final var payload = new AuthPayload("token", user);

    when(authUseCase.register("alice", "secret")).thenReturn(payload);

    assertThat(resolver.register("alice", "secret")).isEqualTo(payload);

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

    assertThat(resolver.login("alice", "secret")).isEqualTo(payload);

    verify(authUseCase).login("alice", "secret");
    verifyNoMoreInteractions(authUseCase);
  }

  @Test
  void shouldLogout() {
    assertThat(resolver.logout()).isTrue();
  }
}
