package com.jobtracker.cli.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtracker.cli.session.SessionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GraphqlClient {
  private final String serverUrl;
  private final SessionManager sessionManager;
  private final HttpClient httpClient;
  private final ObjectMapper mapper;

  public GraphqlClient(@Value("${server.url:http://localhost:8880}") final String serverUrl,
                       final SessionManager sessionManager) {
    this.serverUrl = serverUrl + "/graphql";
    this.sessionManager = sessionManager;
    this.httpClient = HttpClient.newHttpClient();
    this.mapper = new ObjectMapper();
  }

  public JsonNode execute(final String query, final Map<String, Object> variables) {
    try {
      final var body = mapper.writeValueAsString(Map.of("query", query, "variables", variables));
      final var requestBuilder = HttpRequest.newBuilder()
        .uri(URI.create(serverUrl))
        .header("Content-Type", "application/json");
      final var token = sessionManager.loadToken();
      if (!token.isBlank()) {
        requestBuilder.header("Authorization", "Bearer " + token);
      }
      final var request = requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body)).build();
      final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      return mapper.readTree(response.body());
    } catch (Exception e) {
      throw new RuntimeException("GraphQL request failed: " + e.getMessage(), e);
    }
  }
}
