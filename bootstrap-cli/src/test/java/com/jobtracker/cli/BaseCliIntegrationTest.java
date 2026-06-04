package com.jobtracker.cli;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.client.ApacheHttpClientFactory;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.shell.test.ShellTestClient;
import org.springframework.shell.test.autoconfigure.ShellTestClientAutoConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(
  classes = JobTrackerCliApplication.class,
  properties = { "spring.shell.interactive.enabled=false" }
)
@ImportAutoConfiguration(ShellTestClientAutoConfiguration.class)
abstract class BaseCliIntegrationTest {

  private static final WireMockServer wireMockServer;

  static {
    wireMockServer = new WireMockServer(new WireMockConfiguration()
      .dynamicPort()
      .extensions(new ApacheHttpClientFactory()));
    wireMockServer.start();
    WireMock.configureFor("localhost", wireMockServer.port());
    Runtime.getRuntime().addShutdownHook(new Thread(wireMockServer::stop));
  }

  @Autowired
  protected ShellTestClient shell;

  @DynamicPropertySource
  static void configure(DynamicPropertyRegistry registry) {
    registry.add("server.url", () -> "http://localhost:" + wireMockServer.port() + "/api");
  }

  @BeforeEach
  void setUpBase() {
    WireMock.configureFor("localhost", wireMockServer.port());
    stubFor(post(urlPathEqualTo("/api/graphql"))
      .withRequestBody(containing("login"))
      .willReturn(aResponse()
        .withHeader("Content-Type", "application/json")
        .withBody("""
          {"data": {"login": {"token": "test-jwt-token", "user": {"id": "1", "username": "preloaded"}}}}
          """)));
  }

  protected void authenticate() throws Exception {
    WireMock.configureFor("localhost", wireMockServer.port());
    shell.sendCommand("login --username preloaded --password pass");
  }

  protected static void stubGraphql(final String operationName, final String responseBody) {
    WireMock.configureFor("localhost", wireMockServer.port());
    stubFor(post(urlPathEqualTo("/api/graphql"))
      .withRequestBody(containing(operationName))
      .willReturn(aResponse()
        .withHeader("Content-Type", "application/json")
        .withBody(responseBody)));
  }

  protected static void stubGraphqlError() {
    WireMock.configureFor("localhost", wireMockServer.port());
    stubFor(post(urlPathEqualTo("/api/graphql"))
      .withRequestBody(containing("login"))
      .willReturn(aResponse()
        .withStatus(200)
        .withHeader("Content-Type", "application/json")
        .withBody("""
          {"errors": [{"message": "%s"}]}
          """.formatted("Invalid credentials"))));
  }
}
