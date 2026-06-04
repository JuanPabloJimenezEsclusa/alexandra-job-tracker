package com.jobtracker.auth;

import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Intercepts GraphQL requests to resolve JWT authentication.
 */
@Component
public class GraphQlAuthInterceptor implements WebGraphQlInterceptor {
  private final JwtProvider jwtProvider;

  /**
   * Instantiates a new Graph ql auth interceptor.
   */
  public GraphQlAuthInterceptor(final JwtProvider jwtProvider) {
    this.jwtProvider = jwtProvider;
  }

  @Override
  public Mono<WebGraphQlResponse> intercept(final WebGraphQlRequest request, final Chain chain) {
    final var authHeader = request.getHeaders().getFirst("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ") && authHeader.length() > 7) {
      final var userId = jwtProvider.validateToken(authHeader.substring(7));
      request.configureExecutionInput((_, builder) ->
        builder.graphQLContext(ctx -> ctx.put("userId", userId)).build());
    }
    return chain.next(request);
  }
}
