package dev.jpje.jobtracker.events.api;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SqsEventEnvelope(@JsonProperty("Records") List<SqsEventRecord> records) {

  public SqsEventEnvelope {
    Objects.requireNonNull(records, "records must not be null");
  }
}
