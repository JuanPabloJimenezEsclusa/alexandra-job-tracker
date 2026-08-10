package dev.jpje.jobtracker.ai;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class OpenAiSdkRuntimeHints implements RuntimeHintsRegistrar {

  private static final String CONFIG = "META-INF/native-image/reflect-config.json";
  private static final List<String> PACKAGES = List.of(
    "com.openai.core",
    "com.openai.client",
    "com.openai.errors",
    "com.openai.models.chat",
    "com.openai.models.completions"
  );

  @Override
  public void registerHints(final RuntimeHints hints, @Nullable final ClassLoader classLoader) {
    for (final String name : declaredTypes(classLoader)) {
      hints.reflection().registerTypeIfPresent(
        classLoader,
        name,
        MemberCategory.INVOKE_DECLARED_METHODS,
        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS
      );
    }
  }

  private static Set<String> declaredTypes(@Nullable final ClassLoader classLoader) {
    final var names = new LinkedHashSet<String>();
    if (classLoader == null) {
      return names;
    }
    final var mapper = JsonMapper.builder().build();
    try {
      final var resources = classLoader.getResources(CONFIG);
      while (resources.hasMoreElements()) {
        try (final var stream = resources.nextElement().openStream()) {
          final var entries = mapper.readValue(stream, new TypeReference<List<Map<String, Object>>>() {});
          for (final var entry : entries) {
            if (entry.get("name") instanceof final String name && PACKAGES.stream().anyMatch(name::startsWith)) {
              names.add(name);
            }
          }
        }
      }
    } catch (IOException | RuntimeException _) {
      return Set.of();
    }
    return names;
  }
}
