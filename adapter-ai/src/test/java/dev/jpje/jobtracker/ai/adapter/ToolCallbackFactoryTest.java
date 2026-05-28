package dev.jpje.jobtracker.ai.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springaicommunity.agent.tools.SkillsTool;
import org.springframework.core.io.ClassPathResource;

@ExtendWith(MockitoExtension.class)
class ToolCallbackFactoryTest {

  @Mock
  private SkillsLoader skillsLoader;

  @Test
  void shouldBuildToolCallbackWithSkills() {
    // Given
    final var skills = List.of(
      skill("job-summary"),
      skill("company-research"));
    when(skillsLoader.loadSkills()).thenReturn(skills);

    // When
    final var factory = new ToolCallbackFactory(skillsLoader,
      new ClassPathResource("prompts/tool-skill-description.st"));
    final var callback = factory.createToolCallback();

    // Then
    assertThat(callback).as("tool callback built").isNotNull();
    assertThat(callback.getToolDefinition().name()).as("tool name").isEqualTo("Skill");
    assertThat(callback.getToolDefinition().description()).as("tool description lists skills")
      .contains("job-summary", "company-research");
  }

  @Test
  void shouldBuildToolCallbackWithEmptySkills() {
    // Given
    when(skillsLoader.loadSkills()).thenReturn(List.of());

    // When
    final var factory = new ToolCallbackFactory(skillsLoader,
      new ClassPathResource("prompts/tool-skill-description.st"));
    final var callback = factory.createToolCallback();

    // Then
    assertThat(callback).as("tool callback built").isNotNull();
    assertThat(callback.getToolDefinition().name()).as("tool name").isEqualTo("Skill");
  }

  private static SkillsTool.Skill skill(final String name) {
    return new SkillsTool.Skill("skills/" + name,
      java.util.Map.of("name", name, "description", "Test skill " + name),
      "# " + name + "\n\nTest content.");
  }
}
