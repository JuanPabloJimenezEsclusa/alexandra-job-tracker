package dev.jpje.jobtracker.application.port.outbound;

public interface PasswordEncoderPort {
  String encode(String rawPassword);
}
