package com.jobtracker.application.service;

import java.time.Clock;
import java.util.Optional;

import com.jobtracker.domain.model.AuthPayload;
import com.jobtracker.domain.model.User;
import com.jobtracker.domain.port.in.AuthenticationUseCase;
import com.jobtracker.domain.port.out.LoadUserPort;
import com.jobtracker.domain.port.out.SaveUserPort;
import com.jobtracker.domain.port.out.TokenGeneratorPort;
import com.jobtracker.domain.vo.UserId;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Implementation of AuthenticationUseCase with bcrypt password hashing.
 */
public class AuthenticationUseCaseImpl implements AuthenticationUseCase {
  private final SaveUserPort saveUserPort;
  private final LoadUserPort loadUserPort;
  private final TokenGeneratorPort tokenGenerator;
  private final Clock clock;

  /**
   * Constructor.
   */
  public AuthenticationUseCaseImpl(final SaveUserPort saveUserPort,
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
