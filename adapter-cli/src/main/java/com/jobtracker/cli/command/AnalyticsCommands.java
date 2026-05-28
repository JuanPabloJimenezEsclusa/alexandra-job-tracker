package com.jobtracker.cli.command;

import java.util.Map;

import com.jobtracker.cli.client.GraphqlClient;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Shell commands for application analytics.
 */
@ShellComponent
public class AnalyticsCommands {
  private final GraphqlClient client;

  /**
   * Constructs analytics commands with the given GraphQL client.
   */
  public AnalyticsCommands(final GraphqlClient client) {
    this.client = client;
  }

  /**
   * Shows application analytics, optionally filtered by date.
   */
  @ShellMethod(key = {"analytics", "an"}, value = "Show application analytics", group = "Analytics")
  public String analytics(@ShellOption(value = {"--since", "-s"}, defaultValue = "") final String since) {
    var result = client.execute("""
        query($s: Instant) {
          analytics(since: $s) {
            totalApplications conversionRate perStatus {
              saved applied interviewing offer accepted rejected withdrawn
            }
          }
        }""",
      Map.of("s", since));
    return "Analytics: " + result.get("data").get("analytics");
  }
}
