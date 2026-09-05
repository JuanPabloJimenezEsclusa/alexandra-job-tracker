package dev.jpje.jobtracker.api.error;

import java.util.Map;
import java.util.UUID;

import dev.jpje.jobtracker.domain.exception.DomainException;
import dev.jpje.jobtracker.domain.exception.ForbiddenException;
import dev.jpje.jobtracker.domain.exception.InvalidStateTransitionException;
import dev.jpje.jobtracker.domain.exception.OptimisticLockException;
import dev.jpje.jobtracker.domain.exception.ResourceAlreadyExistsException;
import dev.jpje.jobtracker.domain.exception.ResourceNotFoundException;
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
  private static final String INTERNAL_ERROR_MESSAGE = "Internal server error";

  @Override
  protected GraphQLError resolveToSingleError(final Throwable ex, final DataFetchingEnvironment env) {
    var errorId = UUID.randomUUID().toString();
    var errorCode = resolveErrorCode(ex);

    log.error("errorId={} operation={} path={} code={} message={}",
      errorId, env.getOperationDefinition().getName(),
      env.getExecutionStepInfo().getPath(), errorCode, ex.getMessage(), ex);

    return GraphqlErrorBuilder.newError(env)
      .message(resolveMessage(ex))
      .errorType(resolveErrorType(ex))
      .extensions(Map.of(
        "errorCode", errorCode,
        "errorId", errorId,
        "classification", resolveClassification(ex)
      ))
      .build();
  }

  private String resolveMessage(final Throwable ex) {
    return switch (ex) {
      case ResourceNotFoundException e -> e.getMessage() != null ? e.getMessage() : "Resource not found";
      case ResourceAlreadyExistsException e -> e.getMessage() != null ? e.getMessage() : "Resource already exists";
      case OptimisticLockException e -> e.getMessage() != null ? e.getMessage() : "Resource was modified concurrently";
      case InvalidStateTransitionException e -> e.getMessage() != null ? e.getMessage() : "Invalid state transition";
      case ForbiddenException e -> e.getMessage() != null ? e.getMessage() : "Forbidden";
      case IllegalArgumentException e -> e.getMessage() != null ? e.getMessage() : "Invalid input";
      case IllegalStateException e -> e.getMessage() != null ? e.getMessage() : "Invalid state";
      case NullPointerException e -> e.getMessage() != null ? e.getMessage() : "Invalid input";
      default -> INTERNAL_ERROR_MESSAGE;
    };
  }

  private String resolveErrorCode(final Throwable ex) {
    return switch (ex) {
      case ResourceNotFoundException _ -> "NOT_FOUND";
      case ResourceAlreadyExistsException _, OptimisticLockException _ -> "CONFLICT";
      case InvalidStateTransitionException _ -> "INVALID_STATE";
      case ForbiddenException _ -> "FORBIDDEN";
      case IllegalArgumentException _ -> "BAD_REQUEST";
      case IllegalStateException _ -> "INVALID_STATE";
      case NullPointerException _ -> "BAD_REQUEST";
      default -> "INTERNAL_ERROR";
    };
  }

  private ErrorType resolveErrorType(final Throwable ex) {
    return switch (ex) {
      case ResourceNotFoundException _,
           ResourceAlreadyExistsException _,
           OptimisticLockException _,
           ForbiddenException _,
           IllegalArgumentException _,
           NullPointerException _ -> ErrorType.ValidationError;
      case InvalidStateTransitionException _,
           IllegalStateException _ -> ErrorType.InvalidSyntax;
      default -> ErrorType.DataFetchingException;
    };
  }

  private String resolveClassification(final Throwable ex) {
    return switch (ex) {
      case DomainException _ -> "DOMAIN";
      case IllegalArgumentException _ -> "VALIDATION";
      case IllegalStateException _ -> "STATE_ERROR";
      case NullPointerException _ -> "VALIDATION";
      default -> "UNEXPECTED";
    };
  }
}
