package dev.jpje.jobtracker.api;

import java.time.Instant;
import java.util.Locale;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
public class GraphQlConfig {

  static final GraphQLScalarType INSTANT = GraphQLScalarType.newScalar()
    .name("Instant")
    .description("java.time.Instant as ISO-8601 string")
    .coercing(new Coercing<Instant, String>() {
      @Override
      public String serialize(final Object dataFetcherResult, final GraphQLContext context, final Locale locale) {
        if (dataFetcherResult instanceof Instant i) {
          return i.toString();
        }
        throw new CoercingSerializeException("Expected Instant");
      }

      @Override
      public Instant parseValue(final Object input, final GraphQLContext context, final Locale locale) {
        if (input instanceof Number n) {
          return Instant.ofEpochMilli(n.longValue());
        }
        if (input instanceof String s) {
          return Instant.parse(s);
        }
        throw new CoercingParseValueException("Expected number or string");
      }

      @Override
      public Instant parseLiteral(final Value<?> input, final CoercedVariables variables,
                                  final GraphQLContext context, final Locale locale) {
        if (input instanceof graphql.language.IntValue n) {
          return Instant.ofEpochMilli(n.getValue().longValue());
        }
        if (input instanceof graphql.language.StringValue sv) {
          return Instant.parse(sv.getValue());
        }
        throw new CoercingParseLiteralException("Expected number or string");
      }
    })
    .build();

  @Bean
  public RuntimeWiringConfigurer runtimeWiringConfigurer() {
    return wiring -> wiring.scalar(INSTANT);
  }
}
