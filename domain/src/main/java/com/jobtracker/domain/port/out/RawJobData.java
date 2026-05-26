package com.jobtracker.domain.port.out;

public record RawJobData(
  String url,
  String title,
  String company,
  String description,
  String source) {
}
