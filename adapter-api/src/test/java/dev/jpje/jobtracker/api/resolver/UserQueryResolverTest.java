package dev.jpje.jobtracker.api.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import dev.jpje.jobtracker.api.dto.UserResponse;
import dev.jpje.jobtracker.domain.model.User;
import dev.jpje.jobtracker.domain.port.inbound.AuthenticationPort;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.domain.vo.Username;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserQueryResolverTest {

  @InjectMocks
  private UserQueryResolver resolver;

  @Mock
  private AuthenticationPort authUseCase;

  @Test
  void shouldReturnCurrentUser() {
    final var userId = new UserId(UUID.randomUUID());
    final var user = Instancio.of(User.class)
      .set(field(User::id), userId)
      .set(field(User::username), Username.of("alice"))
      .set(field(User::passwordHash), "hash")
      .create();

    when(authUseCase.getCurrentUser(userId)).thenReturn(Optional.of(user));

    assertThat(resolver.me(userId))
      .as("current user should be returned")
      .isNotNull()
      .extracting(UserResponse::username).isEqualTo("alice");

    verify(authUseCase, description("current user should be fetched once")).getCurrentUser(userId);
    verifyNoMoreInteractions(authUseCase);
  }

  @Test
  void shouldReturnNullWhenNoUser() {
    final var userId = new UserId(UUID.randomUUID());

    when(authUseCase.getCurrentUser(userId)).thenReturn(Optional.empty());

    assertThat(resolver.me(userId)).as("me should return null when no user is logged in").isNull();

    verify(authUseCase, description("current user should be fetched once")).getCurrentUser(userId);
    verifyNoMoreInteractions(authUseCase);
  }
}
