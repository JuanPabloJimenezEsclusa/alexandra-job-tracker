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
 * Shell commands for submitting job postings.
 */
@Component
public class JobPostingCommands {
  private final GraphqlClient client;
  private final JqProcessor jqProcessor;

  /**
   * Instantiates a new Job posting commands.
   */
  public JobPostingCommands(final GraphqlClient client, final JqProcessor jqProcessor) {
    this.client = client;
    this.jqProcessor = jqProcessor;
  }

  /**
   * Analyze job posting.
   */
  @Command(
    name = "analyze",
    alias = {"anlz"},
    description = "Analyze a job posting.",
    group = "Posting",
    help = """
      Analyzes a job posting by its ID and returns a summary, key skills, and fit score.
      
      Example usage:
        - analyze -i b6124fbc-eaba-4f38-bea5-54bbd88fe19a
        - anlz -i b6124fbc-eaba-4f38-bea5-54bbd88fe19a -j ".summary"
      """)
  public String analyze(
    @Option(
      longName = "id", shortName = 'i',
      description = "Job posting ID to analyze", required = true) final String id,
    @Option(
      longName = "jq", shortName = 'j',
      description = "jq expression to filter output") @Nullable final String jq) {
    final var result = client.execute("""
        mutation($id: ID!) {
          analyzeJobPosting(jobPostingId: $id) { summary skills fitScore }
        }""",
      Map.of("id", id));
    final var data = result.get("data");
    if (data == null || data.isNull()) {
      return result.toPrettyString();
    }
    return jqProcessor.process(data.get("analyzeJobPosting"), jq);
  }

  /**
   * Submit job posting.
   */
  @Command(
    name = "submit-job",
    alias = {"sj"},
    description = "Submit a job posting from raw data.",
    group = "Posting",
    help = """
      Submits a new job posting.
      The system will attempt to parse the job posting and extract relevant information.
      
      Example usage:
        - submit-job -u https://linkedin.com/job123 -t "Tech Lead" -c "META" -s LINKEDIN -d "Job description"
        - sj -u https://indeed.com/job456 -t "Software Engineer" -c "AMZN" -s INDEED -d "Job description"
      """)
  public String submitJob(
    @Option(
      longName = "url", shortName = 'u',
      description = "Job posting URL", required = true) final String url,
    @Option(
      longName = "title", shortName = 't',
      description = "Job title", required = true) final String title,
    @Option(
      longName = "company", shortName = 'c',
      description = "Company name", required = true) final String company,
    @Option(
      longName = "source", shortName = 's',
      description = "Source: LINKEDIN, INDEED, OTHER", required = true) final String source,
    @Option(
      longName = "desc", shortName = 'd',
      description = "Job description (optional)", required = true) final String description) {
    final var variables = new HashMap<String, Object>();
    variables.put("url", url);
    variables.put("title", title);
    variables.put("company", company);
    variables.put("source", source);
    variables.put("desc", description);
    final var result = client.execute("""
        mutation($url: String!, $title: String!, $company: String!, $source: Source!, $desc: String!) {
          submitJobPosting(input: { url: $url, title: $title, company: $company, source: $source, description: $desc })
          { id title company source }
        }""",
      variables);
    final var data = result.get("data");
    if (data == null || data.isNull() || data.get("submitJobPosting").isNull()) {
      return result.toPrettyString();
    }
    return "Submitted: " + data.get("submitJobPosting");
  }

  /**
   * List job postings.
   */
  @Command(
    name = "postings",
    alias = {"po"},
    description = "List job postings.",
    group = "Posting",
    help = """
      Lists job postings, optionally filtered by source.
      
      Example usage:
        - postings
        - po -s LINKEDIN -j ".[].title"
      """)
  public String postings(
    @Option(
      longName = "source", shortName = 's',
      description = "Filter by source: LINKEDIN, INDEED, OTHER") @Nullable final String source,
    @Option(
      longName = "jq", shortName = 'j',
      description = "jq expression to filter output") @Nullable final String jq) {
    final var variables = new HashMap<String, Object>();
    if (source != null) {
      variables.put("s", source);
    }
    final var result = client.execute("""
        query($s: Source) {
          jobPostings(source: $s) { id title description company source url postedAt }
        }""",
      variables);
    final var data = result.get("data");
    if (data == null || data.isNull()) {
      return result.toPrettyString();
    }
    return jqProcessor.process(data.get("jobPostings"), jq);
  }
}
