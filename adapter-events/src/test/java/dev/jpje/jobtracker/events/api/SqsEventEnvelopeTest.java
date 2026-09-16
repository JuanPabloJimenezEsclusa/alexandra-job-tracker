package dev.jpje.jobtracker.events.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class SqsEventEnvelopeTest {

  private static final String TRACKING_ARN = "arn:aws:sqs:eu-west-1:123456789012:ajt-job-tracking";
  private static final String ANALYSIS_ARN = "arn:aws:sqs:eu-west-1:123456789012:ajt-job-analysis";

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void shouldDeserializeSqsEnvelope() {
    final var json = """
      {
        "Records": [
          {"messageId": "m1", "body": "{\\"occurredAt\\":\\"2026-01-01T00:00:00Z\\"}", "eventSourceARN": "%s"},
          {"messageId": "m2", "body": "{\\"occurredAt\\":\\"2026-01-02T00:00:00Z\\"}", "eventSourceARN": "%s"}
        ]
      }
      """.formatted(TRACKING_ARN, ANALYSIS_ARN);

    final var envelope = mapper.readValue(json, SqsEventEnvelope.class);

    assertThat(envelope.records()).hasSize(2);
    assertThat(envelope.records().getFirst().body()).contains("2026-01-01T00:00:00Z");
    assertThat(envelope.records().getFirst().eventSourceArn()).isEqualTo(TRACKING_ARN);
    assertThat(envelope.records().get(1).eventSourceArn()).isEqualTo(ANALYSIS_ARN);
  }

  @Test
  void shouldRoundTripEnvelope() {
    final var envelope = new SqsEventEnvelope(
      java.util.List.of(new SqsEventRecord("{\"occurredAt\":\"2026-01-01T00:00:00Z\"}", TRACKING_ARN)));

    final var json = mapper.writeValueAsString(envelope);
    final var reparsed = mapper.readValue(json, SqsEventEnvelope.class);

    assertThat(reparsed).isEqualTo(envelope);
  }
}
