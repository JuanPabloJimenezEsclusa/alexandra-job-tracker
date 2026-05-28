package dev.jpje.jobtracker.api.resolver;

import java.util.Objects;

import dev.jpje.jobtracker.api.dto.UserResponse;
import dev.jpje.jobtracker.domain.port.in.AuthenticationPort;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class UserQueryResolver {
  private final AuthenticationPort authUseCase;

  public UserQueryResolver(final AuthenticationPort authUseCase) {
    this.authUseCase = authUseCase;
  }

  @QueryMapping
  @Nullable
  public UserResponse me(@ContextValue(required = false) @Nullable final UserId userId) {
    Objects.requireNonNull(userId, "Authentication required");
    return authUseCase.getCurrentUser(userId)
      .map(UserResponse::from)
      .orElse(null);
  }
}
