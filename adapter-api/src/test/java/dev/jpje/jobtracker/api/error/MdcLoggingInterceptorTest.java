package dev.jpje.jobtracker.api.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.MDC;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

class MdcLoggingInterceptorTest {

  private static final int REQUEST_ID_LENGTH = 8;
  private static final int TRUNCATED_OPERATION_LENGTH = 80;

  private MdcLoggingInterceptor interceptor;

  private static Stream<Arguments> requestIdScenarios() {
    return Stream.of(
      arguments(named("short document", new HttpHeaders()), "{ me }"),
      arguments(named("long document", new HttpHeaders()), longDocument())
    );
  }

  private static Stream<Arguments> hasAuthScenarios() {
    final var headersWithAuth = new HttpHeaders();
    headersWithAuth.setBearerAuth("some-token");
    return Stream.of(
      arguments(named("with bearer token", headersWithAuth), "true"),
      arguments(named("without bearer token", new HttpHeaders()), null)
    );
  }

  private static String longDocument() {
    return """
      mutation {
        createApplication(company: "VeryLongCompanyNameThatExceedsTheMaxLength", role: "Engineer", source: LINKEDIN) { id }
      }
      """;
  }

  @BeforeEach
  void setUp() {
    interceptor = new MdcLoggingInterceptor();
    MDC.clear();
  }

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  @ParameterizedTest(name = "{0} → MDC[requestId]")
  @MethodSource("requestIdScenarios")
  void shouldSetRequestId(final HttpHeaders headers, final String document) {
    // Given
    final var captured = new AtomicReference<@Nullable String>();

    // When
    interceptor.intercept(mockRequest(headers, document), capturingChain(captured, "requestId")).block();

    // Then
    assertThat(captured.get()).hasSize(REQUEST_ID_LENGTH);
  }

  @Test
  void shouldSetOperation() {
    // Given
    final var document = "{ me { username } }";
    final var captured = new AtomicReference<@Nullable String>();

    // When
    interceptor.intercept(mockRequest(new HttpHeaders(), document), capturingChain(captured, "operation")).block();

    // Then
    assertThat(captured.get()).isEqualTo(document);
  }

  @Test
  void shouldTruncateLongOperation() {
    // Given
    final var captured = new AtomicReference<@Nullable String>();

    // When
    interceptor.intercept(mockRequest(new HttpHeaders(), longDocument()), capturingChain(captured, "operation")).block();

    // Then
    assertThat(captured.get()).hasSize(TRUNCATED_OPERATION_LENGTH);
  }

  @ParameterizedTest(name = "{0} → MDC[hasAuth]={1}")
  @MethodSource("hasAuthScenarios")
  void shouldSetHasAuth(final HttpHeaders headers, final String expectedValue) {
    // Given
    final var captured = new AtomicReference<@Nullable String>();

    // When
    interceptor.intercept(mockRequest(headers, "{ me }"), capturingChain(captured, "hasAuth")).block();

    // Then
    assertThat(captured.get()).isEqualTo(expectedValue);
  }

  @Test
  void shouldClearMdcAfterRequest() {
    // Given
    MDC.put("preExisting", "value");

    // When
    interceptor.intercept(mockRequest(new HttpHeaders(), "{ me }"), passingChain()).block();

    // Then
    assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
  }

  private WebGraphQlRequest mockRequest(final HttpHeaders headers, final String document) {
    var req = mock(WebGraphQlRequest.class);
    when(req.getHeaders()).thenReturn(headers);
    when(req.getDocument()).thenReturn(document);
    when(req.getUri()).thenReturn(UriComponentsBuilder.fromUriString("http://localhost:8880/api/graphql").build());
    return req;
  }

  private WebGraphQlInterceptor.Chain capturingChain(final AtomicReference<@Nullable String> captured, final String mdcKey) {
    var chain = mock(WebGraphQlInterceptor.Chain.class);
    when(chain.next(any())).thenAnswer(_ -> {
      captured.set(MDC.get(mdcKey));
      return Mono.just(mock(WebGraphQlResponse.class));
    });
    return chain;
  }

  private WebGraphQlInterceptor.Chain passingChain() {
    var chain = mock(WebGraphQlInterceptor.Chain.class);
    when(chain.next(any())).thenReturn(Mono.just(mock(WebGraphQlResponse.class)));
    return chain;
  }
}
