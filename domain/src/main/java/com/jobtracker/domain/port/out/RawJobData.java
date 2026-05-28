package com.jobtracker.domain.port.out;

/**
 * Raw scraped data from a job posting source.
 */
public record RawJobData(
  String url,
  String title,
  String company,
  String description,
  String source) {
}
