package dev.jpje.jobtracker.api.config;

import graphql.analysis.FieldComplexityCalculator;
import graphql.analysis.MaxQueryComplexityInstrumentation;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import graphql.scalars.ExtendedScalars;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
public class GraphQlConfig {

  @Bean
  public Instrumentation maxQueryDepthInstrumentation() {
    return new MaxQueryDepthInstrumentation(4);
  }

  @Bean
  public Instrumentation maxQueryComplexityInstrumentation() {
    final int MAX_ALIASES = 5;
    final int ALIAS_PENALTY = 1000;
    final int MAX_COMPLEXITY = MAX_ALIASES * ALIAS_PENALTY;

    final FieldComplexityCalculator aliasAwareCalculator = (environment, childComplexity) -> {
      int currentFieldCost = 1;
      if (environment.getField().getAlias() != null) {
        currentFieldCost += ALIAS_PENALTY;
      }
      return currentFieldCost + childComplexity;
    };

    return new MaxQueryComplexityInstrumentation(MAX_COMPLEXITY, aliasAwareCalculator);
  }

  @Bean
  public RuntimeWiringConfigurer runtimeWiringConfigurer() {
    return wiring -> wiring.scalar(ExtendedScalars.DateTime);
  }
}
