package dev.jpje.jobtracker.api.interceptor;

import dev.jpje.jobtracker.domain.exception.InvalidTokenException;
import dev.jpje.jobtracker.domain.port.outbound.TokenGeneratorPort;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class GraphQlAuthInterceptor implements WebGraphQlInterceptor {
  private final TokenGeneratorPort tokenGenerator;

  public GraphQlAuthInterceptor(final TokenGeneratorPort tokenGenerator) {
    this.tokenGenerator = tokenGenerator;
  }

  @Override
  public Mono<WebGraphQlResponse> intercept(final WebGraphQlRequest request, final Chain chain) {
    final var authHeader = request.getHeaders().getFirst("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ") && authHeader.length() > 7) {
      try {
        final var payload = tokenGenerator.validateToken(authHeader.substring(7));
        request.configureExecutionInput((_, builder) ->
          builder.graphQLContext(ctx -> {
            ctx.put("userId", payload.userId());
            ctx.put("userRole", payload.role());
          }).build());
      } catch (final InvalidTokenException _) {
        // Treat an expired or invalid token as unauthenticated: leave the context empty.
      }
    }
    return chain.next(request);
  }
}
