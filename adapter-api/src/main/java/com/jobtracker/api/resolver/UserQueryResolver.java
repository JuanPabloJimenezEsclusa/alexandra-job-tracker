package com.jobtracker.api.resolver;

import java.util.Objects;

import com.jobtracker.api.dto.UserResponse;
import com.jobtracker.domain.port.in.AuthenticationUseCase;
import com.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/**
 * Resolves user profile GraphQL queries.
 */
@Controller
public class UserQueryResolver {
  private final AuthenticationUseCase authUseCase;

  /**
   * Constructor.
   */
  public UserQueryResolver(final AuthenticationUseCase authUseCase) {
    this.authUseCase = authUseCase;
  }

  /**
   * Returns the currently authenticated user.
   */
  @QueryMapping
  @Nullable
  public UserResponse me(@ContextValue(required = false) @Nullable final UserId userId) {
    Objects.requireNonNull(userId, "Authentication required");
    return authUseCase.getCurrentUser(userId)
      .map(UserResponse::from)
      .orElse(null);
  }
}
