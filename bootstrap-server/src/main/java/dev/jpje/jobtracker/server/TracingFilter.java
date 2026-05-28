package dev.jpje.jobtracker.server;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(OpenTelemetry.class)
public class TracingFilter implements Filter {

  private final Tracer tracer;

  public TracingFilter(final OpenTelemetry openTelemetry) {
    this.tracer = openTelemetry.getTracer("job-tracker-server");
  }

  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response,
                       final FilterChain chain) throws IOException, ServletException {
    if (!(request instanceof final HttpServletRequest httpRequest)) {
      chain.doFilter(request, response);
      return;
    }

    final var path = httpRequest.getRequestURI();
    final var method = httpRequest.getMethod();
    final var span = tracer.spanBuilder(method + " " + path)
      .setSpanKind(SpanKind.SERVER)
      .setAttribute("http.method", method)
      .setAttribute("http.url", httpRequest.getRequestURL().toString())
      .setAttribute("http.path", path)
      .startSpan();

    try (final var _ = span.makeCurrent()) {
      chain.doFilter(request, response);
      if (response instanceof final HttpServletResponse httpResponse) {
        span.setAttribute("http.status_code", httpResponse.getStatus());
      }
    } catch (final Exception e) {
      span.recordException(e);
      span.setAttribute("error", true);
      throw e;
    } finally {
      span.end();
    }
  }
}
