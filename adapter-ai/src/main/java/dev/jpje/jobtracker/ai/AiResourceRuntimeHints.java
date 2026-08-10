package dev.jpje.jobtracker.ai;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class AiResourceRuntimeHints implements RuntimeHintsRegistrar {

  @Override
  public void registerHints(final RuntimeHints hints, @Nullable final ClassLoader classLoader) {
    hints.resources().registerPattern("prompts/*.st");
    hints.resources().registerPattern("skills/**/SKILL.md");
  }
}
