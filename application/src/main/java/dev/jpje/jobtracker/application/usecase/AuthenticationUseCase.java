package dev.jpje.jobtracker.application.usecase;

import java.time.Clock;
import java.util.Optional;

import dev.jpje.jobtracker.domain.model.AuthPayload;
import dev.jpje.jobtracker.domain.model.User;
import dev.jpje.jobtracker.domain.port.in.AuthenticationPort;
import dev.jpje.jobtracker.domain.port.out.LoadUserPort;
import dev.jpje.jobtracker.domain.port.out.SaveUserPort;
import dev.jpje.jobtracker.domain.port.out.TokenGeneratorPort;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.mindrot.jbcrypt.BCrypt;

public class AuthenticationUseCase implements AuthenticationPort {
  private final SaveUserPort saveUserPort;
  private final LoadUserPort loadUserPort;
  private final TokenGeneratorPort tokenGenerator;
  private final Clock clock;

  public AuthenticationUseCase(final SaveUserPort saveUserPort,
                               final LoadUserPort loadUserPort,
                               final TokenGeneratorPort tokenGenerator,
                               final Clock clock) {
    this.saveUserPort = saveUserPort;
    this.loadUserPort = loadUserPort;
    this.tokenGenerator = tokenGenerator;
    this.clock = clock;
  }

  @Override
  public AuthPayload register(final String username, final String password) {
    if (loadUserPort.findByUsername(username).isPresent()) {
      throw new IllegalArgumentException("Username already taken");
    }
    final var user = new User(UserId.generate(), username, BCrypt.hashpw(password, BCrypt.gensalt()), clock.instant());
    saveUserPort.save(user);
    return new AuthPayload(tokenGenerator.generateToken(user.id()), user);
  }

  @Override
  public AuthPayload login(final String username, final String password) {
    final var user = loadUserPort.findByUsername(username)
      .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
    if (!BCrypt.checkpw(password, user.passwordHash())) {
      throw new IllegalArgumentException("Invalid credentials");
    }
    return new AuthPayload(tokenGenerator.generateToken(user.id()), user);
  }

  @Override
  public Optional<User> getCurrentUser(final UserId userId) {
    return loadUserPort.findById(userId);
  }
}
