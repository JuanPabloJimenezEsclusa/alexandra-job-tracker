package dev.jpje.jobtracker.auth.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import dev.jpje.jobtracker.auth.exception.KeyDerivationException;
import org.jspecify.annotations.Nullable;

public final class JwtKeyMaterial {
  private static final int MIN_SECRET_LENGTH = 32;
  private static final String HMAC_ALGORITHM = "HmacSHA512";

  private JwtKeyMaterial() {
  }

  public static SecretKey derive(@Nullable final String secret) {
    if (secret == null || secret.isBlank() || secret.length() < MIN_SECRET_LENGTH) {
      throw new IllegalArgumentException("jwt.secret must be set to at least 32 characters");
    }
    try {
      final var digest = MessageDigest.getInstance("SHA-512");
      return new SecretKeySpec(digest.digest(secret.getBytes(StandardCharsets.UTF_8)), HMAC_ALGORITHM);
    } catch (final NoSuchAlgorithmException e) {
      throw new KeyDerivationException("SHA-512 not available", e);
    }
  }
}
