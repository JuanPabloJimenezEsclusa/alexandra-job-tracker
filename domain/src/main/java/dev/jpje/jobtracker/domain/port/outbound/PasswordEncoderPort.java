package dev.jpje.jobtracker.domain.port.outbound;

public interface PasswordEncoderPort {
  String encode(String rawPassword);
  boolean matches(String rawPassword, String encodedPassword);
}
