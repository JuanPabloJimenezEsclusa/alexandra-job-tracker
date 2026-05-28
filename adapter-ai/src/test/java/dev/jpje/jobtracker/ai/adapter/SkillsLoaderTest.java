package dev.jpje.jobtracker.ai.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

@ExtendWith(MockitoExtension.class)
class SkillsLoaderTest {

  private static final int EXPECTED_SKILLS_COUNT = 2;

  @Mock
  private ResourcePatternResolver resolver;

  @Test
  void shouldLoadAllSkills() throws Exception {
    final var resources = new Resource[] {
      new ClassPathResource("skills/job-summary/SKILL.md"),
      new ClassPathResource("skills/company-research/SKILL.md")
    };
    when(resolver.getResources(SkillsLoader.SKILL_PATTERN)).thenReturn(resources);

    final var skills = new SkillsLoader(resolver).loadSkills();

    assertThat(skills).as("all skill resources loaded").hasSize(EXPECTED_SKILLS_COUNT);
    assertThat(skills).as("loaded skill names")
      .extracting("name").containsExactlyInAnyOrder("job-summary", "company-research");
  }
}
