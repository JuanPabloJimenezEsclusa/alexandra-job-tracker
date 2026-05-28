package com.jobtracker.cli.command;

import java.util.HashMap;
import java.util.Map;

import com.jobtracker.cli.client.GraphqlClient;
import org.jspecify.annotations.Nullable;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Shell commands for tracking job applications.
 */
@ShellComponent
public class TrackCommands {
  private final GraphqlClient client;

  /**
   * Constructs track commands with the given GraphQL client.
   */
  public TrackCommands(final GraphqlClient client) {
    this.client = client;
  }

  /**
   * Adds a new job application.
   */
  @ShellMethod(key = {"add", "a"}, value = "Add a job application", group = "Tracking")
  public String add(
    @ShellOption(value = {"--company", "-c"}) final String company,
    @ShellOption(value = {"--role", "-r"}) final String role,
    @ShellOption(value = {"--source", "-s"}) final String source,
    @ShellOption(value = {"--url", "-u"}, defaultValue = ShellOption.NULL) @Nullable final String url,
    @ShellOption(value = {"--notes", "-n"}, defaultValue = ShellOption.NULL) @Nullable final String notes) {

    final var variables = new HashMap<String, Object>();
    variables.put("c", company);
    variables.put("r", role);
    variables.put("s", source);
    if (url != null) {
      variables.put("u", url);
    }
    if (notes != null) {
      variables.put("n", notes);
    }
    final var result = client.execute("""
        mutation($c: String!, $r: String!, $s: Source!, $u: String, $n: String) {
          createApplication(company: $c, role: $r, source: $s, postingUrl: $u, notes: $n) { id status }
        }""",
      variables);

    return "Created: " + result.get("data").get("createApplication");
  }

  /**
   * Lists applications, optionally filtered by status or source.
   */
  @ShellMethod(key = {"list", "l"}, value = "List applications", group = "Tracking")
  public String list(
    @ShellOption(value = {"--status", "-s"}, defaultValue = ShellOption.NULL) @Nullable final String status,
    @ShellOption(value = {"--source"}, defaultValue = ShellOption.NULL) @Nullable final String source) {

    final var variables = new HashMap<String, Object>();
    if (status != null) {
      variables.put("s", status);
    }
    if (source != null) {
      variables.put("src", source);
    }

    final var result = client.execute("""
        query($s: ApplicationStatus, $src: Source) {
          applications(status: $s, source: $src) {
            id, company, role, source, postingUrl, status, dateApplied, lastUpdated, notes
          }
        }""",
      variables);

    return result.get("data").get("applications").toPrettyString();
  }

  /**
   * Updates the status of an application.
   */
  @ShellMethod(key = {"update", "u"}, value = "Update application status", group = "Tracking")
  public String update(
    @ShellOption(value = {"--id", "-i"}) final String id,
    @ShellOption(value = {"--status", "-s"}) final String status,
    @ShellOption(value = {"--notes", "-n"}, defaultValue = ShellOption.NULL) final String notes) {

    final var result = client.execute("""
        mutation($id: ID!, $s: ApplicationStatus!, $n: String) {
          updateApplicationStatus(id: $id, status: $s, notes: $n) { id status lastUpdated }
        }""",
      Map.of("id", id, "s", status, "n", notes));
    return "Updated: " + result.get("data").get("updateApplicationStatus");
  }

  /**
   * Deletes an application by ID.
   */
  @ShellMethod(key = {"delete", "d"}, value = "Delete an application", group = "Tracking")
  public String delete(@ShellOption(value = {"--id", "-i"}) final String id) {
    client.execute("""
        mutation($id: ID!) {
          deleteApplication(id: $id)
        }""",
      Map.of("id", id));
    return "Deleted application " + id;
  }
}
