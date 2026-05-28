package dev.jpje.jobtracker.auth;

import dev.jpje.jobtracker.domain.port.out.PasswordEncoderPort;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordEncoder implements PasswordEncoderPort {
  @Override
  public String encode(final String rawPassword) {
    return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
  }

  @Override
  public boolean matches(final String rawPassword, final String encodedPassword) {
    return BCrypt.checkpw(rawPassword, encodedPassword);
  }
}
