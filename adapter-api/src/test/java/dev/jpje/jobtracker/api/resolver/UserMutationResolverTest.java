package dev.jpje.jobtracker.api.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import dev.jpje.jobtracker.api.dto.AuthPayloadResponse;
import dev.jpje.jobtracker.domain.model.User;
import dev.jpje.jobtracker.domain.port.in.AuthenticationPort;
import dev.jpje.jobtracker.domain.vo.AuthPayload;
import dev.jpje.jobtracker.domain.vo.Username;
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
    stubRegistration(registrationPayload());

    final var result = resolver.register("alice", "secret");

    assertThat(result)
      .extracting(AuthPayloadResponse::token, r -> r.user().username())
      .containsExactly("token", "alice");
    verifyRegistration();
  }

  @Test
  void shouldLogin() {
    stubLogin(registrationPayload());

    final var result = resolver.login("alice", "secret");

    assertThat(result)
      .extracting(AuthPayloadResponse::token, r -> r.user().username())
      .containsExactly("token", "alice");
    verifyLogin();
  }

  @Test
  void shouldLogout() {
    assertThat(resolver.logout()).isTrue();
  }

  private void stubRegistration(final AuthPayload payload) {
    when(authUseCase.register(Username.of("alice"), "secret")).thenReturn(payload);
  }

  private void stubLogin(final AuthPayload payload) {
    when(authUseCase.login(Username.of("alice"), "secret")).thenReturn(payload);
  }

  private void verifyRegistration() {
    verify(authUseCase).register(Username.of("alice"), "secret");
    verifyNoMoreInteractions(authUseCase);
  }

  private void verifyLogin() {
    verify(authUseCase).login(Username.of("alice"), "secret");
    verifyNoMoreInteractions(authUseCase);
  }

  private static AuthPayload registrationPayload() {
    final var user = Instancio.of(User.class)
      .set(field(User::username), Username.of("alice"))
      .set(field(User::passwordHash), "hash")
      .create();
    return new AuthPayload("token", user);
  }
}
