package com.jobtracker.cli.client;

/**
 * Exception thrown when a GraphQL client operation fails.
 */
public class GraphqlClientException extends RuntimeException {
  /**
   * Instantiates a new Graphql client exception.
   */
  public GraphqlClientException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
