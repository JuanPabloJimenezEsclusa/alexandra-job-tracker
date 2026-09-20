package dev.jpje.jobtracker.auth.adapter;

import java.util.Objects;

import dev.jpje.jobtracker.application.port.outbound.AuthenticateUserPort;
import dev.jpje.jobtracker.auth.userdetails.AuthUserDetails;
import dev.jpje.jobtracker.domain.model.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationManagerAdapter implements AuthenticateUserPort {
  private final AuthenticationManager authenticationManager;

  public AuthenticationManagerAdapter(final AuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;
  }

  @Override
  public User authenticate(final String username, final String rawPassword) {
    try {
      final var authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(username, rawPassword));
      return ((AuthUserDetails) Objects.requireNonNull(authentication.getPrincipal())).user();
    } catch (final AuthenticationException e) {
      throw new IllegalArgumentException("Invalid credentials", e);
    }
  }
}
