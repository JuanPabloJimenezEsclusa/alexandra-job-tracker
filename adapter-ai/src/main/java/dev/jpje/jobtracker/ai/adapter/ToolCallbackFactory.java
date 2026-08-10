package dev.jpje.jobtracker.ai.adapter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springaicommunity.agent.tools.SkillsTool;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class ToolCallbackFactory {
  private final SkillsLoader skillsLoader;
  private final Resource toolDescriptionResource;

  public ToolCallbackFactory(final SkillsLoader skillsLoader,
                             @Value("classpath:prompts/tool-skill-description.st") final Resource toolDescriptionResource) {
    this.skillsLoader = skillsLoader;
    this.toolDescriptionResource = toolDescriptionResource;
  }

  public ToolCallback createToolCallback() {
    final var skills = skillsLoader.loadSkills();
    final var skillsMap = skills.stream()
      .collect(Collectors.toMap(SkillsTool.Skill::name, Function.identity()));
    final var skillsXml = skills.stream()
      .map(SkillsTool.Skill::toXml)
      .collect(Collectors.joining("\n"));
    return FunctionToolCallback.builder("Skill", new SkillsTool.SkillsFunction(skillsMap))
      .description(readText(toolDescriptionResource).formatted(skillsXml))
      .inputType(SkillsTool.SkillsInput.class)
      .build();
  }

  private static String readText(final Resource resource) {
    try (var inputStream = resource.getInputStream()) {
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (final IOException e) {
      throw new UncheckedIOException("Failed to read resource: " + resource, e);
    }
  }
}
