package dev.jpje.jobtracker.api.error;

import java.util.Map;
import java.util.UUID;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;

@Component
public class GraphQlExceptionResolver extends DataFetcherExceptionResolverAdapter {

  private static final Logger log = LoggerFactory.getLogger(GraphQlExceptionResolver.class);

  @Override
  protected GraphQLError resolveToSingleError(final Throwable ex, final DataFetchingEnvironment env) {
    var errorId = UUID.randomUUID().toString();
    var errorCode = resolveErrorCode(ex);

    log.error("errorId={} operation={} path={} code={} message={}",
      errorId, env.getOperationDefinition().getName(),
      env.getExecutionStepInfo().getPath(), errorCode, ex.getMessage(), ex);

    return GraphqlErrorBuilder.newError(env)
      .message(ex.getMessage() != null ? ex.getMessage() : "Internal server error")
      .errorType(resolveErrorType(ex))
      .extensions(Map.of(
        "errorCode", errorCode,
        "errorId", errorId,
        "classification", resolveClassification(ex)
      ))
      .build();
  }

  private String resolveErrorCode(final Throwable ex) {
    return switch (ex) {
      case IllegalArgumentException _ -> "BAD_REQUEST";
      case IllegalStateException _ -> "INVALID_STATE";
      case NullPointerException _ -> "BAD_REQUEST";
      default -> "INTERNAL_ERROR";
    };
  }

  private ErrorType resolveErrorType(final Throwable ex) {
    return switch (ex) {
      case IllegalArgumentException _ -> ErrorType.ValidationError;
      case IllegalStateException _ -> ErrorType.InvalidSyntax;
      case NullPointerException _ -> ErrorType.ValidationError;
      default -> ErrorType.DataFetchingException;
    };
  }

  private String resolveClassification(final Throwable ex) {
    return switch (ex) {
      case IllegalArgumentException _ -> "VALIDATION";
      case IllegalStateException _ -> "STATE_ERROR";
      case NullPointerException _ -> "VALIDATION";
      default -> "UNEXPECTED";
    };
  }
}
