package dev.jpje.jobtracker.ai.adapter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.springaicommunity.agent.tools.SkillsTool;
import org.springaicommunity.agent.utils.MarkdownParser;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

@Component
public class SkillsLoader {
  static final String SKILL_PATTERN = "classpath*:skills/**/SKILL.md";

  private final ResourcePatternResolver resolver;

  public SkillsLoader(final ResourcePatternResolver resolver) {
    this.resolver = resolver;
  }

  public List<SkillsTool.Skill> loadSkills() {
    try {
      return Arrays.stream(resolver.getResources(SKILL_PATTERN))
        .map(SkillsLoader::parseSkill)
        .toList();
    } catch (final IOException e) {
      throw new UncheckedIOException("Failed to resolve skills", e);
    }
  }

  private static SkillsTool.Skill parseSkill(final Resource resource) {
    final var content = readText(resource);
    final var parser = new MarkdownParser(content);
    final var frontMatter = parser.getFrontMatter();
    final var name = String.valueOf(frontMatter.get("name"));
    return new SkillsTool.Skill(name, frontMatter, parser.getContent());
  }

  private static String readText(final Resource resource) {
    try (var inputStream = resource.getInputStream()) {
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (final IOException e) {
      throw new UncheckedIOException("Failed to read skill: " + resource, e);
    }
  }
}
