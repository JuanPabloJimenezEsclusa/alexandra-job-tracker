package dev.jpje.jobtracker.domain.port.out;

public interface PasswordEncoderPort {
  String encode(String rawPassword);
  boolean matches(String rawPassword, String encodedPassword);
}
