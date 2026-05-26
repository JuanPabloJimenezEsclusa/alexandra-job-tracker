package com.jobtracker.application.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

import com.jobtracker.domain.model.User;
import com.jobtracker.domain.port.in.AuthenticationUseCase;
import com.jobtracker.domain.port.out.LoadUserPort;
import com.jobtracker.domain.port.out.SaveUserPort;
import com.jobtracker.domain.vo.UserId;

public class AuthenticationUseCaseImpl implements AuthenticationUseCase {
  private final SaveUserPort saveUserPort;
  private final LoadUserPort loadUserPort;

  public AuthenticationUseCaseImpl(final SaveUserPort saveUserPort, final LoadUserPort loadUserPort) {
    this.saveUserPort = saveUserPort;
    this.loadUserPort = loadUserPort;
  }

  @Override
  public User register(final String username, final String password) {
    if (loadUserPort.findByUsername(username).isPresent()) {
      throw new IllegalArgumentException("Username already taken");
    }
    final var user = new User(UserId.generate(), username, hashPassword(password), Instant.now());
    saveUserPort.save(user);
    return user;
  }

  @Override
  public User login(final String username, final String password) {
    final var user = loadUserPort.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
    if (!user.passwordHash().equals(hashPassword(password))) {
      throw new IllegalArgumentException("Invalid credentials");
    }
    return user;
  }

  private String hashPassword(final String password) {
    try {
      final var digest = MessageDigest.getInstance("SHA-512");
      return HexFormat.of().formatHex(digest.digest(password.getBytes()));
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}
