package dev.jpje.jobtracker.cli.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Function;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jpje.jobtracker.cli.client.GraphqlClient;
import dev.jpje.jobtracker.cli.session.SessionManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthCommandsTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Mock
  private GraphqlClient client;

  @Mock
  private SessionManager session;

  @InjectMocks
  private AuthCommands commands;

  private static Stream<Arguments> successScenarios() {
    return Stream.of(
      arguments(named("register", "{\"data\":{\"register\":{\"token\":\"tok\"}}}"),
        "Registered and logged in as alice",
        (Function<AuthCommands, String>) c -> c.register("alice", "secret")),
      arguments(named("login", "{\"data\":{\"login\":{\"token\":\"tok\"}}}"),
        "Logged in as alice",
        (Function<AuthCommands, String>) c -> c.login("alice", "secret"))
    );
  }

  private static Stream<Arguments> nullDataScenarios() {
    return Stream.of(
      arguments(named("register", "{\"data\":null}"),
        (Function<AuthCommands, String>) c -> c.register("alice", "secret")),
      arguments(named("login", "{\"data\":null}"),
        (Function<AuthCommands, String>) c -> c.login("alice", "secret")),
      arguments(named("register without token", "{\"errors\":[{\"message\":\"Username already taken\"}],\"data\":{}}"),
        (Function<AuthCommands, String>) c -> c.register("alice", "secret"))
    );
  }

  private static Stream<Arguments> missingPasswordScenarios() {
    return Stream.of(
      arguments(named("missing", null)),
      arguments(named("blank", " "))
    );
  }

  private static Stream<Arguments> whoamiScenarios() {
    return Stream.of(
      arguments(named("not logged in", "{\"data\":{\"me\":null}}"), "Not logged in"),
      arguments(named("logged in", "{\"data\":{\"me\":{\"username\":\"alice\"}}}"), "User: alice")
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("successScenarios")
  void shouldAuthenticateAndSaveToken(final String response, final String expected,
                                      final Function<AuthCommands, String> invocation) {
    when(client.execute(anyString(), anyMap())).thenReturn(json(response));

    assertThat(invocation.apply(commands)).isEqualTo(expected);
    verify(session).saveToken("tok");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("nullDataScenarios")
  void shouldReturnResponseWhenDataNull(final String response,
                                        final Function<AuthCommands, String> invocation) {
    when(client.execute(anyString(), anyMap())).thenReturn(json(response));

    assertThat(invocation.apply(commands)).isEqualTo(json(response).toPrettyString());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("missingPasswordScenarios")
  void shouldRequirePasswordWhenNotProvided(final String password) {
    assertThatThrownBy(() -> commands.register("alice", password))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Password required. Use --password/-p in non-interactive mode.");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("whoamiScenarios")
  void shouldResolveCurrentUser(final String response, final String expected) {
    when(client.execute(anyString(), anyMap())).thenReturn(json(response));

    assertThat(commands.whoami()).isEqualTo(expected);
  }

  @Test
  void shouldLogout() {
    assertThat(commands.logout()).isEqualTo("Logged out");
    verify(session).clearToken();
  }

  private static JsonNode json(final String body) {
    try {
      return MAPPER.readTree(body);
    } catch (final JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
