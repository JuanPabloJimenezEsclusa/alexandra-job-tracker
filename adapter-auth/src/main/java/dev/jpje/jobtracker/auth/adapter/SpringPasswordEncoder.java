package dev.jpje.jobtracker.auth.adapter;

import java.util.Objects;

import dev.jpje.jobtracker.application.port.outbound.PasswordEncoderPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SpringPasswordEncoder implements PasswordEncoderPort {
  private final PasswordEncoder delegate;

  public SpringPasswordEncoder(final PasswordEncoder delegate) {
    this.delegate = delegate;
  }

  @Override
  public String encode(final String rawPassword) {
    return Objects.requireNonNull(delegate.encode(rawPassword));
  }
}
