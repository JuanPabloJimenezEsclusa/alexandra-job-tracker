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
 * Shell commands for scraping job postings.
 */
@ShellComponent
public class ScrapeCommands {
  private final GraphqlClient client;
  private final JqProcessor jqProcessor;

  /**
   * Constructs scrape commands with the given GraphQL client.
   */
  public ScrapeCommands(final GraphqlClient client, final JqProcessor jqProcessor) {
    this.client = client;
    this.jqProcessor = jqProcessor;
  }

  /**
   * Analyzes a job posting using AI.
   */
  @ShellMethod(
    key = {"analyze", "anlz"},
    value = """
      Analyze a job posting.
      
      EXAMPLES
        - analyze -i 123e4567-e89b-12d3-a456-426614174000
        - anlz -i 123 -j '.summary'""",
    group = "Scraping")
  public String analyze(
    @ShellOption(
      value = {"--id", "-i"},
      help = "Job posting ID to analyze") final String id,
    @ShellOption(
      value = {"--jq", "-j"},
      defaultValue = ShellOption.NULL,
      help = "jq expression to filter output") @Nullable final String jq) {
    final var result = client.execute("""
        mutation($id: ID!) {
          analyzeJobPosting(jobPostingId: $id) { summary skills fitScore }
        }""",
      Map.of("id", id));
    return jqProcessor.process(result.get("data").get("analyzeJobPosting"), jq);
  }

  /**
   * Submits a job posting from raw data (browser extension, manual entry).
   */
  @ShellMethod(
    key = {"submit-job", "sj"},
    value = """
      Submit a job posting from raw data.
      
      EXAMPLES
        - submit-job --url https://linkedin.com/jobs/123 --title "SWE" --company Acme --source LINKEDIN
        - sj -u https://example.com/job -t "Dev" -c "Corp" -s INDEED -d "Hacker analyst"
        - sj -u https://... -t "Engineer" -c "Inc" -s LINKEDIN --desc "Job description here"
      """,
    group = "Scraping")
  public String submitJob(
    @ShellOption(
      value = {"--url", "-u"},
      help = "Job posting URL") final String url,
    @ShellOption(
      value = {"--title", "-t"},
      help = "Job title") final String title,
    @ShellOption(
      value = {"--company", "-c"},
      help = "Company name") final String company,
    @ShellOption(
      value = {"--source", "-s"},
      help = "Source: LINKEDIN, INDEED, OTHER") final String source,
    @ShellOption(
      value = {"--desc", "-d"},
      defaultValue = ShellOption.NULL,
      help = "Job description (optional)") @Nullable final String description) {
    final var variables = new HashMap<String, Object>();
    variables.put("url", url);
    variables.put("title", title);
    variables.put("company", company);
    variables.put("source", source);
    variables.put("desc", description != null ? description : "");
    final var result = client.execute("""
        mutation($url: String!, $title: String!, $company: String!, $source: Source!, $desc: String) {
          submitJobPosting(input: { url: $url, title: $title, company: $company, source: $source, description: $desc })
          { id title company source }
        }""",
      variables);
    return "Submitted: " + result.get("data").get("submitJobPosting");
  }

  /**
   * Lists scraped job postings, optionally filtered by source.
   */
  @ShellMethod(
    key = {"postings", "po"},
    value = """
      List job postings.
      
      EXAMPLES
        - postings
        - po -s LINKEDIN -j '.[].title'
      """, 
    group = "Scraping")
  public String postings(
    @ShellOption(
      value = {"--source", "-s"},
      defaultValue = ShellOption.NULL,
      help = "Filter by source: LINKEDIN, INDEED, OTHER") @Nullable final String source,
    @ShellOption(
      value = {"--jq", "-j"},
      defaultValue = ShellOption.NULL,
      help = "jq expression to filter output") @Nullable final String jq) {
    final var variables = new HashMap<String, Object>();
    if (source != null) {
      variables.put("s", source);
    }
    final var result = client.execute("""
        query($s: Source) {
          jobPostings(source: $s) { id title description company source url postedAt }
        }""",
      variables);
    return jqProcessor.process(result.get("data").get("jobPostings"), jq);
  }
}
