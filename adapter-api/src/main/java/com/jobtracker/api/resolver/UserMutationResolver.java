package com.jobtracker.api.resolver;

import com.jobtracker.domain.model.AuthPayload;
import com.jobtracker.domain.port.in.AuthenticationUseCase;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

/**
 * Resolves user authentication GraphQL mutations.
 */
@Controller
public class UserMutationResolver {
  private final AuthenticationUseCase authUseCase;

  /**
   * Constructor.
   */
  public UserMutationResolver(final AuthenticationUseCase authUseCase) {
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
   * Logs out the current session.
   */
  @MutationMapping
  public boolean logout() {
    return true;
  }
}
