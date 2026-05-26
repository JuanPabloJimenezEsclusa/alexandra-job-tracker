package com.jobtracker.observability.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.ServiceAttributes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OtelConfig {

  @Bean
  public OpenTelemetry openTelemetry(
    @Value("${otel.exporter.otlp.endpoint:http://localhost:4318}") final String endpoint) {
    final var resource = Resource.create(Attributes.of(ServiceAttributes.SERVICE_NAME, "job-tracker-server"));
    final var spanExporter = OtlpGrpcSpanExporter.builder().setEndpoint(endpoint).build();
    final var tracerProvider = SdkTracerProvider.builder()
      .setResource(resource)
      .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
      .build();
    return OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();
  }
}
