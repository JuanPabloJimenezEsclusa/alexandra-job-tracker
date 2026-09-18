package dev.jpje.jobtracker.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import dev.jpje.jobtracker.domain.vo.UserId;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String MDC_USER_ID = "userId";

  private final JwtDecoder jwtDecoder;

  public JwtAuthenticationFilter(final JwtDecoder jwtDecoder) {
    this.jwtDecoder = jwtDecoder;
  }

  @Override
  protected void doFilterInternal(final HttpServletRequest request,
                                  final HttpServletResponse response,
                                  final FilterChain filterChain) throws ServletException, IOException {
    final var header = request.getHeader(AUTHORIZATION_HEADER);
    if (header != null && header.startsWith(BEARER_PREFIX)
        && header.length() > BEARER_PREFIX.length()
        && SecurityContextHolder.getContext().getAuthentication() == null) {
      try {
        final var jwt = jwtDecoder.decode(header.substring(BEARER_PREFIX.length()));
        final var userId = new UserId(UUID.fromString(Objects.requireNonNull(jwt.getSubject())));
        final var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + jwt.getClaimAsString("role")));
        final var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(userId, null, authorities));
        SecurityContextHolder.setContext(context);
        MDC.put(MDC_USER_ID, userId.value().toString());
      } catch (final JwtException | IllegalArgumentException _) {
        SecurityContextHolder.clearContext();
      }
    }
    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove(MDC_USER_ID);
    }
  }
}
