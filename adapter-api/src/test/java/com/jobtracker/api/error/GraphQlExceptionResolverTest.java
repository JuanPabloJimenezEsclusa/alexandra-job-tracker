package com.jobtracker.api.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import graphql.ErrorType;
import graphql.execution.ResultPath;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.language.SourceLocation;
import graphql.schema.DataFetchingEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GraphQlExceptionResolverTest {

  private GraphQlExceptionResolver resolver;
  private DataFetchingEnvironment env;

  private static Stream<Arguments> exceptionMapping() {
    return Stream.of(
      arguments(new IllegalArgumentException("Invalid input"), ErrorType.ValidationError, "BAD_REQUEST", "VALIDATION"),
      arguments(new IllegalStateException("Invalid state transition"), ErrorType.InvalidSyntax, "INVALID_STATE", "STATE_ERROR"),
      arguments(new RuntimeException("Unexpected failure"), ErrorType.DataFetchingException, "INTERNAL_ERROR", "UNEXPECTED"),
      arguments(new IllegalArgumentException("test"), ErrorType.ValidationError, "BAD_REQUEST", "VALIDATION")
    );
  }

  static Stream<Throwable> nullMessageExceptions() {
    return Stream.of(new NullPointerException());
  }

  @BeforeEach
  void setUp() {
    resolver = new GraphQlExceptionResolver();
    env = mock(DataFetchingEnvironment.class);
    var opDef = mock(OperationDefinition.class);
    when(opDef.getName()).thenReturn("testOperation");
    when(env.getOperationDefinition()).thenReturn(opDef);
    when(env.getExecutionStepInfo()).thenReturn(mock());
    when(env.getExecutionStepInfo().getPath()).thenReturn(ResultPath.rootPath());
    var field = mock(Field.class);
    when(field.getSourceLocation()).thenReturn(new SourceLocation(1, 1));
    when(env.getField()).thenReturn(field);
  }

  @ParameterizedTest(name = "{0} → errorCode={2}")
  @MethodSource("exceptionMapping")
  void shouldMapExceptionToStructuredGraphQLError(
    final Throwable ex, final ErrorType expectedType,
    final String expectedCode, final String expectedClassification) {
    // Given
    var expectedMessage = ex.getMessage() != null ? ex.getMessage() : "Internal server error";

    // When
    var error = resolver.resolveToSingleError(ex, env);

    // Then
    assertThat(error.getMessage()).isEqualTo(expectedMessage);
    assertThat(error.getErrorType()).isEqualTo(expectedType);
    assertThat(error.getExtensions()).containsEntry("errorCode", expectedCode);
    assertThat(error.getExtensions()).containsEntry("classification", expectedClassification);
    assertThat(error.getExtensions()).containsKey("errorId");
    assertThat((String) error.getExtensions().get("errorId")).isNotNull();
  }

  @ParameterizedTest(name = "null message → internal error")
  @MethodSource("nullMessageExceptions")
  void shouldReturnDefaultMessageForNullMessage(final Throwable ex) {
    // Given
    var error = resolver.resolveToSingleError(ex, env);

    // Then
    assertThat(error.getMessage()).isEqualTo("Internal server error");
    assertThat(error.getExtensions()).containsEntry("errorCode", "INTERNAL_ERROR");
  }
}
