package dev.jpje.jobtracker.cli.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jpje.jobtracker.cli.session.SessionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GraphqlClient {
  private final String serverUrl;
  private final SessionManager sessionManager;
  private final HttpClient httpClient;
  private final Duration requestTimeout;
  private final ObjectMapper mapper;

  public GraphqlClient(@Value("${server.url:http://localhost:8880/api}") final String serverUrl,
                       @Value("${server.connect-timeout:5000}") final long connectTimeoutMs,
                       @Value("${server.request-timeout:30000}") final long requestTimeoutMs,
                       final SessionManager sessionManager) {
    this.serverUrl = serverUrl + "/graphql";
    this.sessionManager = sessionManager;
    this.httpClient = HttpClient.newBuilder()
      .connectTimeout(Duration.ofMillis(connectTimeoutMs))
      .build();
    this.requestTimeout = Duration.ofMillis(requestTimeoutMs);
    this.mapper = new ObjectMapper();
  }

  public JsonNode execute(final String query, final Map<String, Object> variables) {
    try {
      final var body = mapper.writeValueAsString(Map.of("query", query, "variables", variables));
      final var requestBuilder = HttpRequest.newBuilder()
        .uri(URI.create(serverUrl))
        .timeout(requestTimeout)
        .header("Content-Type", "application/json");
      final var token = sessionManager.loadToken();
      if (!token.isBlank()) {
        requestBuilder.header("Authorization", "Bearer " + token);
      }
      final var request = requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body)).build();
      final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      return mapper.readTree(response.body());
    } catch (final HttpTimeoutException e) {
      throw new GraphqlClientException("GraphQL request timed out after " + requestTimeout.toMillis() + " ms", e);
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new GraphqlClientException("Interrupted while waiting for request", e);
    } catch (final Exception e) {
      throw new GraphqlClientException("GraphQL request failed: " + e.getMessage(), e);
    }
  }
}
