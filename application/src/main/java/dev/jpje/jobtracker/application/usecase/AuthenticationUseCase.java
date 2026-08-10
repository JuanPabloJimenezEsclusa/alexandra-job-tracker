package dev.jpje.jobtracker.application.usecase;

import java.time.Clock;
import java.util.Optional;

import dev.jpje.jobtracker.domain.event.EventPublisher;
import dev.jpje.jobtracker.domain.event.UserRegistered;
import dev.jpje.jobtracker.domain.exception.ResourceAlreadyExistsException;
import dev.jpje.jobtracker.domain.exception.ResourceNotFoundException;
import dev.jpje.jobtracker.domain.model.User;
import dev.jpje.jobtracker.domain.port.in.AuthenticationPort;
import dev.jpje.jobtracker.domain.port.out.LoadUserPort;
import dev.jpje.jobtracker.domain.port.out.PasswordEncoderPort;
import dev.jpje.jobtracker.domain.port.out.SaveUserPort;
import dev.jpje.jobtracker.domain.port.out.TokenGeneratorPort;
import dev.jpje.jobtracker.domain.vo.AuthPayload;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.domain.vo.Username;

public class AuthenticationUseCase implements AuthenticationPort {
  private final SaveUserPort saveUserPort;
  private final LoadUserPort loadUserPort;
  private final TokenGeneratorPort tokenGenerator;
  private final PasswordEncoderPort passwordEncoder;
  private final Clock clock;
  private final EventPublisher eventPublisher;

  public AuthenticationUseCase(final SaveUserPort saveUserPort,
                               final LoadUserPort loadUserPort,
                               final TokenGeneratorPort tokenGenerator,
                               final PasswordEncoderPort passwordEncoder,
                               final Clock clock,
                               final EventPublisher eventPublisher) {
    this.saveUserPort = saveUserPort;
    this.loadUserPort = loadUserPort;
    this.tokenGenerator = tokenGenerator;
    this.passwordEncoder = passwordEncoder;
    this.clock = clock;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public AuthPayload register(final Username username, final String password) {
    if (loadUserPort.findByUsername(username.value()).isPresent()) {
      throw new ResourceAlreadyExistsException("Username already taken");
    }
    final var user = new User(UserId.generate(), username, passwordEncoder.encode(password), clock.instant());
    saveUserPort.save(user);
    eventPublisher.publish(new UserRegistered(user.id(), username, clock.instant()));
    return new AuthPayload(tokenGenerator.generateToken(user.id()), user);
  }

  @Override
  public AuthPayload login(final Username username, final String password) {
    final var user = loadUserPort.findByUsername(username.value())
      .orElseThrow(() -> new ResourceNotFoundException("Invalid credentials"));
    if (!passwordEncoder.matches(password, user.passwordHash())) {
      throw new IllegalArgumentException("Invalid credentials");
    }
    return new AuthPayload(tokenGenerator.generateToken(user.id()), user);
  }

  @Override
  public Optional<User> getCurrentUser(final UserId userId) {
    return loadUserPort.findById(userId);
  }
}
