package dev.jpje.jobtracker.api.resolver;

import dev.jpje.jobtracker.api.dto.UserResponse;
import dev.jpje.jobtracker.api.error.ForbiddenException;
import dev.jpje.jobtracker.domain.port.inbound.AuthenticationPort;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
public class UserQueryResolver {
  private final AuthenticationPort authUseCase;

  public UserQueryResolver(final AuthenticationPort authUseCase) {
    this.authUseCase = authUseCase;
  }

  @QueryMapping
  public UserResponse me(@AuthenticationPrincipal @Nullable final UserId userId) {
    if (userId == null) {
      throw new ForbiddenException("Authentication required");
    }
    return authUseCase.getCurrentUser(userId)
      .map(UserResponse::from)
      .orElseThrow(() -> new ForbiddenException("Authentication required"));
  }
}
