package com.jobtracker.cli.command;

import java.util.Map;

import com.jobtracker.cli.client.GraphqlClient;
import com.jobtracker.cli.format.JqProcessor;
import org.jspecify.annotations.Nullable;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Shell commands for application analytics.
 */
@ShellComponent
public class AnalyticsCommands {
  private final GraphqlClient client;
  private final JqProcessor jqProcessor;

  /**
   * Constructs analytics commands with the given GraphQL client.
   */
  public AnalyticsCommands(final GraphqlClient client, final JqProcessor jqProcessor) {
    this.client = client;
    this.jqProcessor = jqProcessor;
  }

  /**
   * Shows application analytics, optionally filtered by date.
   */
  @ShellMethod(key = {"analytics", "an"}, value = "Show application analytics", group = "Analytics")
  public String analytics(
    @ShellOption(value = {"--since", "-s"}, defaultValue = "") final String since,
    @ShellOption(value = {"--jq", "-j"}, defaultValue = ShellOption.NULL) @Nullable final String jq) {
    var result = client.execute("""
        query($s: Instant) {
          analytics(since: $s) {
            totalApplications conversionRate perStatus {
              saved applied interviewing offer accepted rejected withdrawn
            }
          }
        }""",
      Map.of("s", since));
    return "Analytics: " + jqProcessor.process(result.get("data").get("analytics"), jq);
  }
}
