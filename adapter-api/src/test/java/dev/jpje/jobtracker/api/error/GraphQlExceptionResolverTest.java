package dev.jpje.jobtracker.api.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import dev.jpje.jobtracker.domain.exception.ForbiddenException;
import dev.jpje.jobtracker.domain.exception.InvalidStateTransitionException;
import dev.jpje.jobtracker.domain.exception.OptimisticLockException;
import dev.jpje.jobtracker.domain.exception.ResourceAlreadyExistsException;
import dev.jpje.jobtracker.domain.exception.ResourceNotFoundException;
import graphql.ErrorType;
import graphql.GraphQLError;
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
      arguments(named("resource not found", new ResourceNotFoundException("Application not found")),
        "Application not found", ErrorType.ValidationError, "NOT_FOUND", "DOMAIN"),
      arguments(named("resource already exists", new ResourceAlreadyExistsException("Username already taken")),
        "Username already taken", ErrorType.ValidationError, "CONFLICT", "DOMAIN"),
      arguments(named("optimistic lock conflict",
          new OptimisticLockException("Application was modified concurrently", new IllegalStateException())),
        "Application was modified concurrently", ErrorType.ValidationError, "CONFLICT", "DOMAIN"),
      arguments(named("invalid state transition", new InvalidStateTransitionException("Invalid transition")),
        "Invalid transition", ErrorType.InvalidSyntax, "INVALID_STATE", "DOMAIN"),
      arguments(named("forbidden", new ForbiddenException("Admin access required")),
        "Admin access required", ErrorType.ValidationError, "FORBIDDEN", "DOMAIN"),
      arguments(named("illegal argument", new IllegalArgumentException("Invalid input")),
        "Invalid input", ErrorType.ValidationError, "BAD_REQUEST", "VALIDATION"),
      arguments(named("illegal state", new IllegalStateException("Invalid state transition")),
        "Invalid state transition", ErrorType.InvalidSyntax, "INVALID_STATE", "STATE_ERROR"),
      arguments(named("runtime failure", new RuntimeException("Unexpected failure")),
        "Internal server error", ErrorType.DataFetchingException, "INTERNAL_ERROR", "UNEXPECTED"),
      arguments(named("null pointer", new NullPointerException("Authentication required")),
        "Authentication required", ErrorType.ValidationError, "BAD_REQUEST", "VALIDATION"),
      arguments(named("null message", new NullPointerException()),
        "Invalid input", ErrorType.ValidationError, "BAD_REQUEST", "VALIDATION")
    );
  }

  @BeforeEach
  void setUp() {
    resolver = new GraphQlExceptionResolver();
    env = mock(DataFetchingEnvironment.class);

    final var opDef = mock(OperationDefinition.class);
    final var executionStepInfo = mock(graphql.execution.ExecutionStepInfo.class);
    final var field = mock(Field.class);

    when(opDef.getName()).thenReturn("testOperation");
    when(env.getOperationDefinition()).thenReturn(opDef);
    when(env.getExecutionStepInfo()).thenReturn(executionStepInfo);
    when(env.getExecutionStepInfo().getPath()).thenReturn(ResultPath.rootPath());
    when(field.getSourceLocation()).thenReturn(new SourceLocation(1, 1));
    when(env.getField()).thenReturn(field);
  }

  @ParameterizedTest(name = "{0} → errorCode={3}")
  @MethodSource("exceptionMapping")
  void shouldMapExceptionToStructuredGraphQLError(
    final Throwable ex, final String expectedMessage,
    final ErrorType expectedType, final String expectedCode, final String expectedClassification) {
    // When
    final var error = resolver.resolveToSingleError(ex, env);

    // Then
    assertThat(error)
      .extracting(
        GraphQLError::getMessage,
        GraphQLError::getErrorType,
        e -> e.getExtensions().get("errorCode"),
        e -> e.getExtensions().get("classification"),
        e -> e.getExtensions().containsKey("errorId"))
      .containsExactly(expectedMessage, expectedType, expectedCode, expectedClassification, true);
  }
}
