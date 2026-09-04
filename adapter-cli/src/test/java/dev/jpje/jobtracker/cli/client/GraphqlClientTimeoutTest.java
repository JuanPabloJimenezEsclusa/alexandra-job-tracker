package dev.jpje.jobtracker.cli.client;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.InetSocketAddress;
import java.util.Map;

import com.sun.net.httpserver.HttpServer;
import dev.jpje.jobtracker.cli.session.SessionManager;
import org.junit.jupiter.api.Test;

class GraphqlClientTimeoutTest {

  @Test
  void shouldFailWithClearErrorWhenServerDoesNotRespondInTime() throws Exception {
    final var server = HttpServer.create(new InetSocketAddress(0), 0);
    try {
      server.createContext("/api/graphql", exchange -> {
        try {
          Thread.sleep(2000L);
        } catch (final InterruptedException e) {
          Thread.currentThread().interrupt();
        }
        try {
          final var body = "{}".getBytes();
          exchange.sendResponseHeaders(200, body.length);
          exchange.getResponseBody().write(body);
        } catch (final Exception _) {
          // the client has already timed out and closed the connection
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
      server.stop(0);
    }
  }
}
