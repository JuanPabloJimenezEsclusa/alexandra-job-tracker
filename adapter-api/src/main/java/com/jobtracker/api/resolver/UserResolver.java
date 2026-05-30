package com.jobtracker.api.resolver;

import com.jobtracker.domain.model.AuthPayload;
import com.jobtracker.domain.model.User;
import com.jobtracker.domain.port.in.AuthenticationUseCase;
import com.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/**
 * Resolves user authentication and profile GraphQL queries and mutations.
 */
@Controller
public class UserResolver {
  private final AuthenticationUseCase authUseCase;

  /**
   * Constructor.
   */
  public UserResolver(final AuthenticationUseCase authUseCase) {
    this.authUseCase = authUseCase;
  }

  /**
   * Registers a new user account.
   */
  @MutationMapping
  public AuthPayload register(@Argument final String username,
                              @Argument final String password) {
    return authUseCase.register(username, password);
  }

  /**
   * Authenticates a user and returns an auth token.
   */
  @MutationMapping
  public AuthPayload login(@Argument final String username,
                           @Argument final String password) {
    return authUseCase.login(username, password);
  }

  /**
   * Returns the currently authenticated user.
   */
  @QueryMapping
  @Nullable
  public User me(@ContextValue final UserId userId) {
    return authUseCase.getCurrentUser(userId).orElse(null);
  }

  /**
   * Logs out the current session.
   */
  @MutationMapping
  public boolean logout() {
    return true;
  }
}
