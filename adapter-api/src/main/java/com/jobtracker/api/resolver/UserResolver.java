package com.jobtracker.api.resolver;

import com.jobtracker.api.dto.AuthPayload;
import com.jobtracker.application.service.AuthenticationUseCaseImpl;
import com.jobtracker.auth.JwtProvider;
import com.jobtracker.domain.model.User;
import com.jobtracker.domain.port.in.AuthenticationUseCase;
import com.jobtracker.domain.port.out.LoadUserPort;
import com.jobtracker.domain.port.out.SaveUserPort;
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
  private final JwtProvider jwtProvider;
  private final LoadUserPort loadUserPort;

  /**
   * Constructor.
   */
  public UserResolver(final SaveUserPort saveUserPort,
                      final LoadUserPort loadUserPort,
                      final JwtProvider jwtProvider) {
    this.authUseCase = new AuthenticationUseCaseImpl(saveUserPort, loadUserPort);
    this.jwtProvider = jwtProvider;
    this.loadUserPort = loadUserPort;
  }

  /**
   * Registers a new user account.
   */
  @MutationMapping
  public AuthPayload register(@Argument final String username,
                              @Argument final String password) {
    final var user = authUseCase.register(username, password);
    final var token = jwtProvider.generateToken(user.id());
    return new AuthPayload(token, user);
  }

  /**
   * Authenticates a user and returns an auth token.
   */
  @MutationMapping
  public AuthPayload login(@Argument final String username,
                           @Argument final String password) {
    final var user = authUseCase.login(username, password);
    final var token = jwtProvider.generateToken(user.id());
    return new AuthPayload(token, user);
  }

  /**
   * Returns the currently authenticated user.
   */
  @QueryMapping
  @Nullable
  public User me(@ContextValue final UserId userId) {
    return loadUserPort.findById(userId).orElse(null);
  }

  /**
   * Logs out the current session.
   */
  @MutationMapping
  public boolean logout() {
    return true;
  }
}
