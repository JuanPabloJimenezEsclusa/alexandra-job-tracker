package com.jobtracker.cli.command;

import java.util.HashMap;
import java.util.Map;

import com.jobtracker.cli.client.GraphqlClient;
import com.jobtracker.cli.format.JqProcessor;
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
  private final JqProcessor jqProcessor;

  /**
   * Constructs track commands with the given GraphQL client.
   */
  public TrackCommands(final GraphqlClient client, final JqProcessor jqProcessor) {
    this.client = client;
    this.jqProcessor = jqProcessor;
  }

  /**
   * Adds a new job application.
   */
  @ShellMethod(
    key = {"add", "a"},
    value = """
      Add a job application.
      
      EXAMPLES
        - add -c Acme -r Engineer -s LINKEDIN
        - add -c Google -r SRE -s INDEED --url https://careers.google.com""",
    group = "Tracking")
  public String add(
    @ShellOption(
      value = {"--company", "-c"},
      help = "Company name") final String company,
    @ShellOption(
      value = {"--role", "-r"},
      help = "Job role/title") final String role,
    @ShellOption(
      value = {"--source", "-s"},
      help = "Source: LINKEDIN, INDEED, OTHER") final String source,
    @ShellOption(
      value = {"--url", "-u"},
      defaultValue = ShellOption.NULL,
      help = "Posting URL (optional)") @Nullable final String url,
    @ShellOption(
      value = {"--notes", "-n"},
      defaultValue = ShellOption.NULL,
      help = "Notes (optional)") @Nullable final String notes) {

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
  @ShellMethod(
    key = {"list", "l"},
    value = """
      List applications.
      
      EXAMPLES
        - list
        - list -s APPLIED
        - list --source LINKEDIN
        - l -j '.[] | {role,company,status}'
        - l -j '.[] | select((.company == "ACME") and (.status == "SAVED")) | {id,role,postingUrl}'""",
    group = "Tracking")
  public String list(
    @ShellOption(
      value = {"--status", "-s"},
      defaultValue = ShellOption.NULL,
      help = "Filter by status: SAVED, APPLIED, INTERVIEWING, OFFER, ACCEPTED, REJECTED, WITHDRAWN") @Nullable final String status,
    @ShellOption(
      value = {"--source"},
      defaultValue = ShellOption.NULL,
      help = "Filter by source: LINKEDIN, INDEED, OTHER") @Nullable final String source,
    @ShellOption(
      value = {"--jq", "-j"},
      defaultValue = ShellOption.NULL,
      help = "jq expression to filter output (e.g., '.[].company')") @Nullable final String jq) {

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

    return jqProcessor.process(result.get("data").get("applications"), jq);
  }

  /**
   * Updates the status of an application.
   */
  @ShellMethod(
    key = {"update", "u"},
    value = """
      Update application status.
      
      EXAMPLES
        - update -i b6124fbc-eaba-4f38-bea5-54bbd88fe19a -s WITHDRAWN
        - u -i b6124fbc-eaba-4f38-bea5-54bbd88fe19a -s APPLIED -n 'Followed up via email on May 3rd'""",
    group = "Tracking")
  public String update(
    @ShellOption(
      value = {"--id", "-i"},
      help = "Application ID") final String id,
    @ShellOption(
      value = {"--status", "-s"},
      help = "New status: SAVED, APPLIED, INTERVIEWING, OFFER, ACCEPTED, REJECTED, WITHDRAWN") final String status,
    @ShellOption(
      value = {"--notes", "-n"},
      defaultValue = ShellOption.NULL,
      help = "Notes (optional)") @Nullable final String notes) {

    final var variables = new HashMap<String, Object>();
    variables.put("id", id);
    variables.put("s", status);

    if (notes != null) {
      variables.put("n", notes);
    }

    final var result = client.execute("""
        mutation($id: ID!, $s: ApplicationStatus!, $n: String) {
          updateApplicationStatus(id: $id, status: $s, notes: $n) { id status lastUpdated }
        }""",
      variables);
    return "Updated: " + result.get("data").get("updateApplicationStatus");
  }

  /**
   * Deletes an application by ID.
   */
  @ShellMethod(
    key = {"delete", "d"},
    value = """
      Delete an application.
      
      EXAMPLES
        - d -i b6124fbc-eaba-4f38-bea5-54bbd88fe19a""",
    group = "Tracking")
  public String delete(@ShellOption(
    value = {"--id", "-i"},
    help = "Application ID to delete") final String id) {
    client.execute("""
        mutation($id: ID!) {
          deleteApplication(id: $id)
        }""",
      Map.of("id", id));
    return "Deleted application " + id;
  }
}
