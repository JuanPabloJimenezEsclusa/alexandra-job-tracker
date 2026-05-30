package com.jobtracker.api.resolver;

import com.jobtracker.domain.model.User;
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
  public User me(@ContextValue final UserId userId) {
    return authUseCase.getCurrentUser(userId).orElse(null);
  }
}
