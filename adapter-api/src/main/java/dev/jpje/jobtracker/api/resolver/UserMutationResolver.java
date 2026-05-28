package dev.jpje.jobtracker.api.resolver;

import dev.jpje.jobtracker.api.dto.AuthPayloadResponse;
import dev.jpje.jobtracker.domain.port.in.AuthenticationPort;
import dev.jpje.jobtracker.domain.vo.Username;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class UserMutationResolver {
  private final AuthenticationPort authUseCase;

  public UserMutationResolver(final AuthenticationPort authUseCase) {
    this.authUseCase = authUseCase;
  }

  @MutationMapping
  public AuthPayloadResponse register(@Argument final String username,
                                      @Argument final String password) {
    return AuthPayloadResponse.from(authUseCase.register(Username.of(username), password));
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
