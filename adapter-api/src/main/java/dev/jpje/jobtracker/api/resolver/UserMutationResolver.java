package dev.jpje.jobtracker.api.resolver;

import java.util.Objects;

import dev.jpje.jobtracker.api.Authorization;
import dev.jpje.jobtracker.api.dto.AuthPayloadResponse;
import dev.jpje.jobtracker.domain.port.in.AuthenticationPort;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.domain.vo.UserRole;
import dev.jpje.jobtracker.domain.vo.Username;
import org.jspecify.annotations.Nullable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class UserMutationResolver {
  private final AuthenticationPort authUseCase;

  public UserMutationResolver(final AuthenticationPort authUseCase) {
    this.authUseCase = authUseCase;
  }

  @MutationMapping
  public AuthPayloadResponse register(@ContextValue(required = false) @Nullable final UserId userId,
                                      @ContextValue(required = false) @Nullable final UserRole userRole,
                                      @Argument final String username,
                                      @Argument final String password,
                                      @Argument final UserRole role) {
    Objects.requireNonNull(userId, "Authentication required");
    Authorization.requireAdmin(userRole);
    return AuthPayloadResponse.from(authUseCase.register(Username.of(username), password, role));
  }

  @MutationMapping
  public AuthPayloadResponse login(@Argument final String username,
                                   @Argument final String password) {
    return AuthPayloadResponse.from(authUseCase.login(Username.of(username), password));
  }

  @MutationMapping
  public boolean logout() {
    return true;
  }
}
