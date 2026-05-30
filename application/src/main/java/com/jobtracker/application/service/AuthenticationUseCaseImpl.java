package com.jobtracker.application.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

import com.jobtracker.domain.model.AuthPayload;
import com.jobtracker.domain.model.User;
import com.jobtracker.domain.port.in.AuthenticationUseCase;
import com.jobtracker.domain.port.out.LoadUserPort;
import com.jobtracker.domain.port.out.SaveUserPort;
import com.jobtracker.domain.port.out.TokenGeneratorPort;
import com.jobtracker.domain.vo.UserId;

/**
 * Implementation of AuthenticationUseCase with SHA-512 password hashing.
 */
public class AuthenticationUseCaseImpl implements AuthenticationUseCase {
  private final SaveUserPort saveUserPort;
  private final LoadUserPort loadUserPort;
  private final TokenGeneratorPort tokenGenerator;

  /**
   * Constructor.
   */
  public AuthenticationUseCaseImpl(final SaveUserPort saveUserPort,
                                   final LoadUserPort loadUserPort,
                                   final TokenGeneratorPort tokenGenerator) {
    this.saveUserPort = saveUserPort;
    this.loadUserPort = loadUserPort;
    this.tokenGenerator = tokenGenerator;
  }

  @Override
  public AuthPayload register(final String username, final String password) {
    if (loadUserPort.findByUsername(username).isPresent()) {
      throw new IllegalArgumentException("Username already taken");
    }
    final var user = new User(UserId.generate(), username, hashPassword(password), Instant.now());
    saveUserPort.save(user);
    return new AuthPayload(tokenGenerator.generateToken(user.id()), user);
  }

  @Override
  public AuthPayload login(final String username, final String password) {
    final var user = loadUserPort.findByUsername(username)
      .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
    if (!user.passwordHash().equals(hashPassword(password))) {
      throw new IllegalArgumentException("Invalid credentials");
    }
    return new AuthPayload(tokenGenerator.generateToken(user.id()), user);
  }

  @Override
  public Optional<User> getCurrentUser(final UserId userId) {
    return loadUserPort.findById(userId);
  }

  private String hashPassword(final String password) {
    try {
      final var digest = MessageDigest.getInstance("SHA-512");
      return HexFormat.of().formatHex(digest.digest(password.getBytes()));
    } catch (final NoSuchAlgorithmException e) {
      throw new UnsupportedOperationException("SHA-512 not supported", e);
    }
  }
}
