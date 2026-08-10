package dev.jpje.jobtracker.ai.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springaicommunity.agent.tools.SkillsTool;
import org.springframework.core.io.ClassPathResource;

@ExtendWith(MockitoExtension.class)
class ToolCallbackFactoryTest {

  @Mock
  private SkillsLoader skillsLoader;

  private static Stream<Arguments> skillsScenarios() {
    return Stream.of(
      arguments(named("two skills", List.of(
        skill("job-summary"),
        skill("company-research"))))
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("skillsScenarios")
  void shouldBuildToolCallbackWithSkills(final List<SkillsTool.Skill> skills) {
    when(skillsLoader.loadSkills()).thenReturn(skills);

    final var factory = new ToolCallbackFactory(skillsLoader,
      new ClassPathResource("prompts/tool-skill-description.st"));
    final var callback = factory.createToolCallback();

    assertThat(callback).as("tool callback built").isNotNull();
    assertThat(callback.getToolDefinition().name()).as("tool name").isEqualTo("Skill");
    assertThat(callback.getToolDefinition().description()).as("tool description lists skills")
      .contains("job-summary", "company-research");
  }

  @Test
  void shouldBuildToolCallbackWithEmptySkills() {
    when(skillsLoader.loadSkills()).thenReturn(List.of());

    final var factory = new ToolCallbackFactory(skillsLoader,
      new ClassPathResource("prompts/tool-skill-description.st"));
    final var callback = factory.createToolCallback();

    assertThat(callback).as("tool callback built").isNotNull();
    assertThat(callback.getToolDefinition().name()).as("tool name").isEqualTo("Skill");
  }

  private static SkillsTool.Skill skill(final String name) {
    return new SkillsTool.Skill("skills/" + name,
      java.util.Map.of("name", name, "description", "Test skill " + name),
      "# " + name + "\n\nTest content.");
  }
}
