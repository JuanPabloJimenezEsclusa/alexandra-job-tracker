package dev.jpje.jobtracker.cli.command;

import java.util.HashMap;

import dev.jpje.jobtracker.cli.client.GraphqlClient;
import dev.jpje.jobtracker.cli.format.JqProcessor;
import org.jspecify.annotations.Nullable;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

@Component
public class  AnalyticsCommands {
  private final GraphqlClient client;
  private final JqProcessor jqProcessor;

  public AnalyticsCommands(final GraphqlClient client, final JqProcessor jqProcessor) {
    this.client = client;
    this.jqProcessor = jqProcessor;
  }

  @Command(
    name = "analytics",
    alias = {"an"},
    description = "Show application analytics.",
    group = "Analytics",
    help = """
      Shows analytics about your job applications, including total count, conversion rates, and breakdown by status.
      You can optionally filter analytics to show only applications since a certain date (in ISO-8601 format).
      
      Example usage:
        - analytics
        - an -s 2026-01-01
        - an -s 2026-01-01 -j ".totalApplications"
      """)
  public String analytics(
    @Option(
      longName = "since", shortName = 's',
      description = "Show analytics since this date (ISO-8601, e.g. 2026-01-01)") @Nullable final String since,
    @Option(
      longName = "jq", shortName = 'j',
      description = "jq expression to filter output") @Nullable final String jq) {

    final var variables = new HashMap<String, Object>();
    if (since != null) {
      variables.put("s", since);
    }
    var result = client.execute("""
        query($s: Instant) {
          analytics(since: $s) {
            totalApplications conversionRate perStatus {
              saved applied interviewing offer accepted rejected withdrawn
            }
          }
        }""",
      variables);
    final var data = result.get("data");
    if (data == null || data.isNull()) {
      return result.toPrettyString();
    }
    return "Analytics: " + jqProcessor.process(data.get("analytics"), jq);
  }
}
