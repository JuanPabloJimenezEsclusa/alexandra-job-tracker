package com.jobtracker.cli.command;

import java.util.Map;

import com.jobtracker.cli.client.GraphqlClient;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class TrackCommands {
  private final GraphqlClient client;

  public TrackCommands(GraphqlClient client) {
    this.client = client;
  }

  @ShellMethod(value = "Add a job application", group = "Tracking")
  public String add(
    @ShellOption final String company,
    @ShellOption final String role,
    @ShellOption final String source,
    @ShellOption(defaultValue = "") final String url,
    @ShellOption(defaultValue = "") final String notes) {
    final var result = client.execute("""
      mutation($c: String!, $r: String!, $s: Source!, $u: String, $n: String) {
        createApplication(company: $c, role: $r, source: $s, postingUrl: $u, notes: $n) { id status }
      }""",
      Map.of("c", company, "r", role, "s", source, "u", url, "n", notes));
    return "Created: " + result.get("data").get("createApplication");
  }

  @ShellMethod(value = "List applications", group = "Tracking")
  public String list(
    @ShellOption(defaultValue = "") final String status,
    @ShellOption(defaultValue = "") final String source) {
    final var result = client.execute("""
        query($s: ApplicationStatus, $src: Source) {
          applications(status: $s, source: $src) { id company role status }
        }""",
      Map.of("s", status.isEmpty() ? "null" : status, "src", source.isEmpty() ? "null" : source));
    return result.get("data").get("applications").toPrettyString();
  }

  @ShellMethod(value = "Update application status", group = "Tracking")
  public String update(
    final String id,
    final String status,
    @ShellOption(defaultValue = "") final String notes) {
    final var result = client.execute("""
        mutation($id: ID!, $s: ApplicationStatus!, $n: String) {
          updateApplicationStatus(id: $id, status: $s, notes: $n) { id status lastUpdated }
        }""",
      Map.of("id", id, "s", status, "n", notes));
    return "Updated: " + result.get("data").get("updateApplicationStatus");
  }

  @ShellMethod(value = "Delete an application", group = "Tracking")
  public String delete(final String id) {
    client.execute("""
      mutation($id: ID!) { deleteApplication(id: $id) }""",
      Map.of("id", id));
    return "Deleted application " + id;
  }
}
