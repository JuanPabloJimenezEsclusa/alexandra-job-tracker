package com.jobtracker.api.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.jobtracker.domain.model.User;
import com.jobtracker.domain.port.in.AuthenticationUseCase;
import com.jobtracker.domain.vo.UserId;
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
    final var user = new User(userId, "alice", "hash", Instant.now());

    when(authUseCase.getCurrentUser(userId)).thenReturn(Optional.of(user));

    assertThat(resolver.me(userId))
      .isNotNull()
      .extracting(User::username).isEqualTo("alice");

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
