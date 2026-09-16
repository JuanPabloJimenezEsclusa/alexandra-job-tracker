package dev.jpje.jobtracker.auth.hint;

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

public class JjwtRuntimeHints implements RuntimeHintsRegistrar {

  @Override
  public void registerHints(final RuntimeHints hints, @Nullable final ClassLoader classLoader) {
    registerForBinding(hints);
  }

  private static void registerForBinding(final RuntimeHints hints) {
    for (final Class<?> type : new Class<?>[]{
      DefaultJwtBuilder.class,
      DefaultJwtBuilder.Supplier.class,
      DefaultJwtParserBuilder.class,
      DefaultJwtParserBuilder.Supplier.class,
      DefaultClaims.class,
      DefaultClaimsBuilder.class,
      DefaultClaimsBuilder.Supplier.class,
      DefaultJwtHeaderBuilder.class,
      DefaultJwtHeaderBuilder.Supplier.class,
      DefaultJwsHeader.class,
      DefaultProtectedHeader.class,
      DefaultHeader.class,
      ParameterMap.class,
      Nameable.class,
      ParameterReadable.class,
      KeysBridge.class,
      StandardCompressionAlgorithms.class,
      StandardEncryptionAlgorithms.class,
      StandardKeyAlgorithms.class,
      StandardKeyOperations.class,
      StandardSecureDigestAlgorithms.class,
      DefaultKeyOperationBuilder.class,
      DefaultKeyOperationBuilder.Supplier.class,
      DefaultKeyOperationPolicyBuilder.class,
      DefaultKeyOperationPolicyBuilder.Supplier.class}) {
      hints.reflection().registerType(type, MemberCategory.values());
    }
  }
}
