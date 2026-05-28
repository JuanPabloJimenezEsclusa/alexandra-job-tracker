package dev.jpje.jobtracker.cli.client;

public class GraphqlClientException extends RuntimeException {
  public GraphqlClientException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
