package dev.jpje.jobtracker.cli.command;

import java.util.HashMap;
import java.util.Map;

import dev.jpje.jobtracker.cli.client.GraphqlClient;
import dev.jpje.jobtracker.cli.format.JqProcessor;
import org.jspecify.annotations.Nullable;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

@Component
public class TrackCommands {
  private final GraphqlClient client;
  private final JqProcessor jqProcessor;

  public TrackCommands(final GraphqlClient client, final JqProcessor jqProcessor) {
    this.client = client;
    this.jqProcessor = jqProcessor;
  }

  @Command(
    name = "add",
    alias = {"a"},
    description = "Add a job application.",
    group = "Tracking",
    help = """
      Adds a new job application for an existing job posting, with optional notes.
      
      Example usage:
        - add -i b6124fbc-eaba-4f38-bea5-54bbd88fe19a
        - a -i b6124fbc-eaba-4f38-bea5-54bbd88fe19a -n "Applied on 2026-01-15"
      """)
  public String add(
    @Option(
      longName = "posting-id", shortName = 'i',
      description = "Job posting ID", required = true) final String postingId,
    @Option(
      longName = "notes", shortName = 'n',
      description = "Notes (optional)") @Nullable final String notes) {

    final var variables = new HashMap<String, Object>();
    variables.put("id", postingId);
    if (notes != null) {
      variables.put("n", notes);
    }
    final var result = client.execute("""
        mutation($id: ID!, $n: String) {
          createApplication(jobPostingId: $id, notes: $n) { id jobPostingId status }
        }""",
      variables);

    final var data = result.get("data");
    if (data == null || data.isNull()) {
      return result.toPrettyString();
    }
    return "Created: " + data.get("createApplication");
  }

  @Command(
    name = "list",
    alias = {"l"},
    description = "List applications.",
    group = "Tracking",
    help = """
      Lists job applications, optionally filtered by status.
      
       Example usage:
        - list
        - list -s APPLIED
        - list --source LINKEDIN
        - l -j ".[] | {jobPostingId,status}"
        - l -j ".[] | select(.status == "SAVED") | {id,jobPostingId}"
      """)
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
            id, jobPostingId, status, dateApplied, lastUpdated, notes
          }
        }""",
      variables);

    final var data = result.get("data");
    if (data == null || data.isNull()) {
      return result.toPrettyString();
    }
    return jqProcessor.process(data.get("applications"), jq);
  }

  @Command(
    name = "update",
    alias = {"u"},
    description = "Update application status.",
    group = "Tracking",
    help = """
      Updates the status of an application by ID, with optional notes.
      
       Example usage:
        - update -i b6124fbc-eaba-4f38-bea5-54bbd88fe19a -s WITHDRAWN
        - u -i b6124fbc-eaba-4f38-bea5-54bbd88fe19a -s APPLIED -n "Followed up via email on May 3rd"
      """)
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
    final var data = result.get("data");
    if (data == null || data.isNull()) {
      return result.toPrettyString();
    }
    return "Updated: " + data.get("updateApplicationStatus");
  }

  @Command(
    name = "delete",
    alias = {"d"},
    description = "Delete an application.",
    group = "Tracking",
    help = """
      Deletes an application by ID.
      
       Example usage:
        - delete -i b6124fbc-eaba-4f38-bea5-54bbd88fe19a
        - d -i b6124fbc-eaba-4f38-bea5-54bbd88fe19a
      """)
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
