package dev.jpje.jobtracker.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import dev.jpje.jobtracker.domain.vo.UserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  private static final UserId USER_ID = UserId.generate();

  @Mock
  private JwtDecoder jwtDecoder;

  private JwtAuthenticationFilter filter;

  @BeforeEach
  void setUp() {
    filter = new JwtAuthenticationFilter(jwtDecoder);
  }

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldAuthenticateValidToken() throws Exception {
    when(jwtDecoder.decode("good-token")).thenReturn(jwt());

    filter.doFilter(requestWithToken("good-token"), new MockHttpServletResponse(), new MockFilterChain());

    final var authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).as("valid token should authenticate the request").isNotNull();
    assertThat(authentication.getPrincipal()).isEqualTo(USER_ID);
    assertThat(authentication.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
  }

  @Test
  void shouldIgnoreMissingToken() throws Exception {
    filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), new MockFilterChain());

    assertThat(SecurityContextHolder.getContext().getAuthentication())
      .as("missing token should leave the request unauthenticated").isNull();
  }

  @Test
  void shouldIgnoreMalformedToken() throws Exception {
    when(jwtDecoder.decode("not-a-jwt")).thenThrow(new BadJwtException("malformed"));

    filter.doFilter(requestWithToken("not-a-jwt"), new MockHttpServletResponse(), new MockFilterChain());

    assertThat(SecurityContextHolder.getContext().getAuthentication())
      .as("malformed token should leave the request unauthenticated").isNull();
  }

  @Test
  void shouldIgnoreExpiredToken() throws Exception {
    when(jwtDecoder.decode("expired-token")).thenThrow(new BadJwtException("expired"));

    filter.doFilter(requestWithToken("expired-token"), new MockHttpServletResponse(), new MockFilterChain());

    assertThat(SecurityContextHolder.getContext().getAuthentication())
      .as("expired token should leave the request unauthenticated").isNull();
  }

  private static MockHttpServletRequest requestWithToken(final String token) {
    final var request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + token);
    return request;
  }

  private static Jwt jwt() {
    return Jwt.withTokenValue("token")
      .header("alg", "HS512")
      .subject(JwtAuthenticationFilterTest.USER_ID.value().toString())
      .claim("role", "USER")
      .build();
  }
}
