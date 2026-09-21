package dev.jpje.jobtracker.server.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.atomic.AtomicReference;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class TracingFilterTest {

  private static final String GRAPHQL_URL = "http://localhost:8880/api/graphql";

  private TracingFilter filter;

  @BeforeEach
  void setUp() {
    MDC.clear();
    final OpenTelemetry openTelemetry = OpenTelemetrySdk.builder().build();
    filter = new TracingFilter(openTelemetry);
  }

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  @Test
  void shouldPublishOtelTraceIdInMdc() throws Exception {
    final var request = mockRequest();
    final var response = mock(HttpServletResponse.class);
    final var capturedMdcTraceId = new AtomicReference<@Nullable String>();
    final var capturedSpanTraceId = new AtomicReference<@Nullable String>();
    final FilterChain chain = (servletRequest, servletResponse) -> {
      capturedMdcTraceId.set(MDC.get("traceId"));
      capturedSpanTraceId.set(Span.current().getSpanContext().getTraceId());
    };

    filter.doFilter(request, response, chain);

    assertThat(capturedMdcTraceId.get()).isNotBlank();
    assertThat(capturedMdcTraceId.get()).isEqualTo(capturedSpanTraceId.get());
  }

  @Test
  void shouldRemoveTraceIdFromMdcAfterRequest() throws Exception {
    final var request = mockRequest();
    final var response = mock(HttpServletResponse.class);

    filter.doFilter(request, response, (servletRequest, servletResponse) -> {
    });

    assertThat(MDC.get("traceId")).isNull();
  }

  private HttpServletRequest mockRequest() {
    final var request = mock(HttpServletRequest.class);
    when(request.getMethod()).thenReturn("POST");
    when(request.getRequestURI()).thenReturn("/api/graphql");
    when(request.getRequestURL()).thenReturn(new StringBuffer(GRAPHQL_URL));
    return request;
  }
}
