package dev.jpje.jobtracker.server.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SqsEventRecord(@JsonProperty("body") String body,
                             @JsonProperty("eventSourceARN") @Nullable String eventSourceArn) {

  public SqsEventRecord {
    Objects.requireNonNull(body, "body must not be null");
  }
}
