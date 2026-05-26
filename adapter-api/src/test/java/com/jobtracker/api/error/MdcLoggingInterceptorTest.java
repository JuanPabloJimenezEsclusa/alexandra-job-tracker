package com.jobtracker.api.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

  private MdcLoggingInterceptor interceptor;

  static Stream<Arguments> mdcScenarios() {
    final var shortDoc = "{ me }";
    final var longDoc = "mutation { createApplication(company: \"VeryLongCompanyNameThatExceedsTheMaxLength\", role: \"Engineer\", source: LINKEDIN) { id } }";
    final var headersWithAuth = new HttpHeaders();
    headersWithAuth.setBearerAuth("some-token");
    return Stream.of(
      arguments(named("should set requestId", new HttpHeaders()), shortDoc, "requestId", null),
      arguments(named("should set operation", new HttpHeaders()), "{ me { username } }", "operation", "{ me { username } }"),
      arguments(named("should truncate long operation", new HttpHeaders()), longDoc, "operation", null),
      arguments(named("should set hasAuth when Bearer token present", headersWithAuth), shortDoc, "hasAuth", "true"),
      arguments(named("should not set hasAuth without Bearer token", new HttpHeaders()), shortDoc, "hasAuth", null)
    );
  }

  static Stream<Arguments> clearMdcScenarios() {
    return Stream.of(arguments("preExisting", "value"));
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

  @ParameterizedTest(name = "{0} → MDC[{2}]={3}")
  @MethodSource("mdcScenarios")
  void shouldSetMdcContext(final HttpHeaders headers, final String document,
                           final String mdcKey, final String expectedValue) {
    // Given
    final var captured = new AtomicReference<String>();
    final var chain = capturingChain(captured, mdcKey);

    // When
    interceptor.intercept(mockRequest(headers, document), chain).block();

    // Then
    if (expectedValue != null) {
      if (mdcKey.equals("operation") && captured.get() != null && captured.get().length() == 80) {
        assertThat(captured.get()).hasSize(80);
      } else {
        assertThat(captured.get()).isEqualTo(expectedValue);
      }
    } else if (mdcKey.equals("requestId")) {
      assertThat(captured.get()).isNotNull().hasSize(8);
    } else if (mdcKey.equals("operation")) {
      assertThat(captured.get()).hasSize(80);
    } else {
      assertThat(captured.get()).isNull();
    }
  }

  @ParameterizedTest(name = "should clear MDC after request")
  @MethodSource("clearMdcScenarios")
  void shouldClearMdcAfterRequest(final String preExistingKey, final String preExistingValue) {
    // Given
    MDC.put(preExistingKey, preExistingValue);

    // When
    interceptor.intercept(mockRequest(new HttpHeaders(), "{ me }"), passingChain()).block();

    // Then
    assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
  }

  private WebGraphQlRequest mockRequest(final HttpHeaders headers, final String document) {
    var req = mock(WebGraphQlRequest.class);
    when(req.getHeaders()).thenReturn(headers != null ? headers : new HttpHeaders());
    when(req.getDocument()).thenReturn(document);
    when(req.getUri()).thenReturn(UriComponentsBuilder.fromUriString("http://localhost:8880/api/graphql").build());
    return req;
  }

  private WebGraphQlInterceptor.Chain capturingChain(final AtomicReference<String> captured, final String mdcKey) {
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
