package com.jobtracker.api.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import com.jobtracker.api.dto.UserResponse;
import com.jobtracker.domain.model.User;
import com.jobtracker.domain.port.in.AuthenticationUseCase;
import com.jobtracker.domain.vo.UserId;
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
  private AuthenticationUseCase authUseCase;

  @Test
  void shouldReturnCurrentUser() {
    final var userId = new UserId(UUID.randomUUID());
    final var user = Instancio.of(User.class)
      .set(field(User::id), userId)
      .set(field(User::username), "alice")
      .set(field(User::passwordHash), "hash")
      .create();

    when(authUseCase.getCurrentUser(userId)).thenReturn(Optional.of(user));

    assertThat(resolver.me(userId))
      .isNotNull()
      .extracting(UserResponse::username).isEqualTo("alice");

    verify(authUseCase).getCurrentUser(userId);
    verifyNoMoreInteractions(authUseCase);
  }

  @Test
  void shouldReturnNullWhenNoUser() {
    final var userId = new UserId(UUID.randomUUID());

    when(authUseCase.getCurrentUser(userId)).thenReturn(Optional.empty());

    assertThat(resolver.me(userId)).isNull();

    verify(authUseCase).getCurrentUser(userId);
    verifyNoMoreInteractions(authUseCase);
  }
}
