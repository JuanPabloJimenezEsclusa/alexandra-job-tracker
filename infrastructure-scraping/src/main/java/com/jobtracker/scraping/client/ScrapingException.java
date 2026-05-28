package com.jobtracker.scraping.client;

/**
 * Exception thrown when a scraping operation fails.
 */
public class ScrapingException extends RuntimeException {
  /**
   * Instantiates a new Scraping exception.
   */
  public ScrapingException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
