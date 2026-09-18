package dev.jpje.jobtracker.api.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import dev.jpje.jobtracker.api.dto.AuthPayloadResponse;
import dev.jpje.jobtracker.domain.model.User;
import dev.jpje.jobtracker.domain.port.inbound.AuthenticationPort;
import dev.jpje.jobtracker.domain.vo.AuthPayload;
import dev.jpje.jobtracker.domain.vo.UserRole;
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

    final var result = resolver.register("alice", "secret", UserRole.USER);

    assertThat(result)
      .as("registration should return token and username")
      .extracting(AuthPayloadResponse::token, r -> r.user().username())
      .containsExactly("token", "alice");
    verify(authUseCase, description("registration should be delegated to auth use case"))
      .register(Username.of("alice"), "secret", UserRole.USER);
    verifyNoMoreInteractions(authUseCase);
  }

  @Test
  void shouldLogin() {
    stubLogin(registrationPayload());

    final var result = resolver.login("alice", "secret");

    assertThat(result)
      .as("login should return token and username")
      .extracting(AuthPayloadResponse::token, r -> r.user().username())
      .containsExactly("token", "alice");
    verify(authUseCase, description("login should be delegated to auth use case"))
      .login(Username.of("alice"), "secret");
    verifyNoMoreInteractions(authUseCase);
  }

  @Test
  void shouldLogout() {
    assertThat(resolver.logout()).as("logout always succeeds (stateless session)").isTrue();
    verifyNoInteractions(authUseCase);
  }

  private void stubRegistration(final AuthPayload payload) {
    when(authUseCase.register(Username.of("alice"), "secret", UserRole.USER)).thenReturn(payload);
  }

  private void stubLogin(final AuthPayload payload) {
    when(authUseCase.login(Username.of("alice"), "secret")).thenReturn(payload);
  }

  private static AuthPayload registrationPayload() {
    final var user = Instancio.of(User.class)
      .set(field(User::username), Username.of("alice"))
      .set(field(User::passwordHash), "hash")
      .create();
    return new AuthPayload("token", user);
  }
}
