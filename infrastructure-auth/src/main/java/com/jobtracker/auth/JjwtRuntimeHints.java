package com.jobtracker.auth;

import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.impl.DefaultJwsHeader;
import io.jsonwebtoken.impl.DefaultJwtBuilder;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * Registers AOT runtime hints for JJWT reflection.
 */
public class JjwtRuntimeHints implements RuntimeHintsRegistrar {

  @Override
  public void registerHints(final RuntimeHints hints, @Nullable final ClassLoader classLoader) {
    hints.reflection().registerType(DefaultJwtBuilder.class, MemberCategory.values());
    hints.reflection().registerType(DefaultClaims.class, MemberCategory.values());
    hints.reflection().registerType(DefaultJwsHeader.class, MemberCategory.values());
  }
}
