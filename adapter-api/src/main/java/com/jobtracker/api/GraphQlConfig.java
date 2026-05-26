package com.jobtracker.api;

import java.time.Instant;

import graphql.language.StringValue;
import graphql.schema.Coercing;
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
    .description("java.time.Instant as epoch millis")
    .coercing(new Coercing<Instant, Long>() {
      @Override
      public Long serialize(Object dataFetcherResult) {
        if (dataFetcherResult instanceof Instant i) return i.toEpochMilli();
        throw new CoercingSerializeException("Expected Instant");
      }

      @Override
      public Instant parseValue(Object input) {
        if (input instanceof Number n) return Instant.ofEpochMilli(n.longValue());
        if (input instanceof String s) return Instant.parse(s);
        throw new CoercingParseValueException("Expected number or string");
      }

      @Override
      public Instant parseLiteral(Object input) {
        if (input instanceof Number n) return Instant.ofEpochMilli(n.longValue());
        if (input instanceof StringValue sv) return Instant.parse(sv.getValue());
        return null;
      }
    })
    .build();

  @Bean
  public RuntimeWiringConfigurer runtimeWiringConfigurer() {
    return wiring -> wiring.scalar(INSTANT);
  }
}
