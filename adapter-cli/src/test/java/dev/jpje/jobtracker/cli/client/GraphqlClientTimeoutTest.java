package dev.jpje.jobtracker.cli.client;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sun.net.httpserver.HttpServer;
import dev.jpje.jobtracker.cli.session.SessionManager;
import org.junit.jupiter.api.Test;

class GraphqlClientTimeoutTest {

  @Test
  void shouldFailWithClearErrorWhenServerDoesNotRespondInTime() throws Exception {
    final var server = HttpServer.create(new InetSocketAddress(0), 0);
    final var releaseResponse = new AtomicBoolean(false);
    try {
      server.createContext("/api/graphql", exchange -> {
        await().atMost(Duration.ofSeconds(5)).until(releaseResponse::get);
        try {
          final var body = "{}".getBytes();
          exchange.sendResponseHeaders(200, body.length);
          exchange.getResponseBody().write(body);
        } finally {
          exchange.close();
        }
      });
      server.start();

      final var port = server.getAddress().getPort();
      final var client = new GraphqlClient("http://localhost:" + port + "/api",
        500L, 300L, new SessionManager(System.getProperty("java.io.tmpdir")));

      // When, then
      assertThatThrownBy(() -> client.execute("query { applications { id } }", Map.of()))
        .as("an unresponsive server surfaces a timeout error")
        .isInstanceOf(GraphqlClientException.class)
        .hasMessageContaining("timed out after 300 ms");
    } finally {
      releaseResponse.set(true);
      server.stop(0);
    }
  }
}
