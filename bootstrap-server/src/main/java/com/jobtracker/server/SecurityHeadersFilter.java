package com.jobtracker.server;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

/**
 * The type Security headers filter.
 */
@Component
public class SecurityHeadersFilter implements Filter {

  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response,
                       final FilterChain chain) throws IOException, ServletException {
    if (response instanceof final HttpServletResponse httpResponse) {
      httpResponse.setHeader("X-Content-Type-Options", "nosniff");
    }
    chain.doFilter(request, response);
  }
}
