package com.jobtracker.cli.command;

import java.util.HashMap;
import java.util.Map;

import com.jobtracker.cli.client.GraphqlClient;
import com.jobtracker.cli.format.JqProcessor;
import org.jspecify.annotations.Nullable;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

/**
 * Shell commands for tracking job applications.
 */
@Component
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
  @Command(
    name = "add",
    alias = {"a"},
    description = "Add a job application.",
    group = "Tracking",
    help = """
      Adds a new job application with the specified company, role, source, and optional posting URL and notes.
      
      Example usage:
        - add -c "Acme Corp" -r 'Software Engineer' -s LINKEDIN
        - a -c 'Beta Inc' -r 'Data Scientist' -s INDEED -u 'https://example.com/job/123' -n 'Applied on 2026-01-15'""")
  public String add(
    @Option(
      longName = "company", shortName = 'c',
      description = "Company name", required = true) final String company,
    @Option(
      longName = "role", shortName = 'r',
      description = "Job role/title", required = true) final String role,
    @Option(
      longName = "source", shortName = 's',
      description = "Source: LINKEDIN, INDEED, OTHER", required = true) final String source,
    @Option(
      longName = "url", shortName = 'u',
      description = "Posting URL (optional)") @Nullable final String url,
    @Option(
      longName = "notes", shortName = 'n',
      description = "Notes (optional)") @Nullable final String notes) {

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
  @Command(
    name = "list",
    alias = {"l"},
    description = "List applications.",
    group = "Tracking",
    help = """
      Lists job applications, optionally filtered by status or source.
      
       Example usage:
        - list
        - list -s APPLIED
        - list --source LINKEDIN
        - l -j '.[] | {role,company,status}'
        - l -j '.[] | select((.company == "ACME") and (.status == "SAVED")) | {id,role,postingUrl}'""")
  public String list(
    @Option(
      longName = "status", shortName = 's',
      description = "Filter by status: SAVED, APPLIED, INTERVIEWING, OFFER, ACCEPTED, REJECTED, WITHDRAWN") @Nullable final String status,
    @Option(
      longName = "source",
      description = "Filter by source: LINKEDIN, INDEED, OTHER") @Nullable final String source,
    @Option(
      longName = "jq", shortName = 'j',
      description = "jq expression to filter output (e.g., '.[].company')") @Nullable final String jq) {

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
  @Command(
    name = "update",
    alias = {"u"},
    description = "Update application status.",
    group = "Tracking",
    help = """
      Updates the status of an application by ID, with optional notes.
      
       Example usage:
        - update -i b6124fbc-eaba-4f38-bea5-54bbd88fe19a -s WITHDRAWN
        - u -i b6124fbc-eaba-4f38-bea5-54bbd88fe19a -s APPLIED -n 'Followed up via email on May 3rd'""")
  public String update(
    @Option(
      longName = "id", shortName = 'i',
      description = "Application ID", required = true) final String id,
    @Option(
      longName = "status", shortName = 's',
      description = "New status: SAVED, APPLIED, INTERVIEWING, OFFER, ACCEPTED, REJECTED, WITHDRAWN",
      required = true) final String status,
    @Option(
      longName = "notes", shortName = 'n',
      description = "Notes (optional)") @Nullable final String notes) {

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
  @Command(
    name = "delete",
    alias = {"d"},
    description = "Delete an application.",
    group = "Tracking",
    help = """
      Deletes an application by ID.
      
       Example usage:
        - delete -i b6124fbc-eaba-4f38-bea5-54bbd88fe19a
        - d -i b6124fbc-eaba-4f38-bea5-54bbd88fe19a""")
  public String delete(@Option(
    longName = "id", shortName = 'i',
    description = "Application ID to delete") final String id) {
    client.execute("""
        mutation($id: ID!) {
          deleteApplication(id: $id)
        }""",
      Map.of("id", id));
    return "Deleted application " + id;
  }
}
