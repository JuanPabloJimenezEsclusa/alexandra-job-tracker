package dev.jpje.jobtracker.api.error;

import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MdcLoggingInterceptor implements WebGraphQlInterceptor {

  @Override
  public Mono<WebGraphQlResponse> intercept(final WebGraphQlRequest request, final Chain chain) {
    final var requestId = UUID.randomUUID().toString().substring(0, 8);
    MDC.put("requestId", requestId);

    final var document = request.getDocument();
    if (document.length() > 80) {
      MDC.put("operation", document.substring(0, 80));
    } else {
      MDC.put("operation", document);
    }

    final var authHeader = request.getHeaders().getFirst("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      MDC.put("hasAuth", "true");
    }

    return chain.next(request).doFinally(_ -> MDC.clear());
  }
}
