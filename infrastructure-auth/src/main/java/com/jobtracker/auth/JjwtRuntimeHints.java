package com.jobtracker.auth;

import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.impl.DefaultClaimsBuilder;
import io.jsonwebtoken.impl.DefaultHeader;
import io.jsonwebtoken.impl.DefaultJwsHeader;
import io.jsonwebtoken.impl.DefaultJwtBuilder;
import io.jsonwebtoken.impl.DefaultJwtHeaderBuilder;
import io.jsonwebtoken.impl.DefaultJwtParserBuilder;
import io.jsonwebtoken.impl.DefaultProtectedHeader;
import io.jsonwebtoken.impl.ParameterMap;
import io.jsonwebtoken.impl.io.StandardCompressionAlgorithms;
import io.jsonwebtoken.impl.lang.Nameable;
import io.jsonwebtoken.impl.lang.ParameterReadable;
import io.jsonwebtoken.impl.security.DefaultKeyOperationBuilder;
import io.jsonwebtoken.impl.security.DefaultKeyOperationPolicyBuilder;
import io.jsonwebtoken.impl.security.KeysBridge;
import io.jsonwebtoken.impl.security.StandardEncryptionAlgorithms;
import io.jsonwebtoken.impl.security.StandardKeyAlgorithms;
import io.jsonwebtoken.impl.security.StandardKeyOperations;
import io.jsonwebtoken.impl.security.StandardSecureDigestAlgorithms;
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
    hints.reflection().registerType(DefaultJwtBuilder.Supplier.class, MemberCategory.values());
    hints.reflection().registerType(DefaultJwtParserBuilder.class, MemberCategory.values());
    hints.reflection().registerType(DefaultJwtParserBuilder.Supplier.class, MemberCategory.values());
    hints.reflection().registerType(DefaultClaims.class, MemberCategory.values());
    hints.reflection().registerType(DefaultClaimsBuilder.class, MemberCategory.values());
    hints.reflection().registerType(DefaultClaimsBuilder.Supplier.class, MemberCategory.values());
    hints.reflection().registerType(DefaultJwtHeaderBuilder.class, MemberCategory.values());
    hints.reflection().registerType(DefaultJwtHeaderBuilder.Supplier.class, MemberCategory.values());
    hints.reflection().registerType(DefaultJwsHeader.class, MemberCategory.values());
    hints.reflection().registerType(DefaultProtectedHeader.class, MemberCategory.values());
    hints.reflection().registerType(DefaultHeader.class, MemberCategory.values());
    hints.reflection().registerType(ParameterMap.class, MemberCategory.values());
    hints.reflection().registerType(Nameable.class, MemberCategory.values());
    hints.reflection().registerType(ParameterReadable.class, MemberCategory.values());
    hints.reflection().registerType(KeysBridge.class, MemberCategory.values());
    hints.reflection().registerType(StandardCompressionAlgorithms.class, MemberCategory.values());
    hints.reflection().registerType(StandardEncryptionAlgorithms.class, MemberCategory.values());
    hints.reflection().registerType(StandardKeyAlgorithms.class, MemberCategory.values());
    hints.reflection().registerType(StandardKeyOperations.class, MemberCategory.values());
    hints.reflection().registerType(StandardSecureDigestAlgorithms.class, MemberCategory.values());
    hints.reflection().registerType(DefaultKeyOperationBuilder.class, MemberCategory.values());
    hints.reflection().registerType(DefaultKeyOperationBuilder.Supplier.class, MemberCategory.values());
    hints.reflection().registerType(DefaultKeyOperationPolicyBuilder.class, MemberCategory.values());
    hints.reflection().registerType(DefaultKeyOperationPolicyBuilder.Supplier.class, MemberCategory.values());
  }
}
