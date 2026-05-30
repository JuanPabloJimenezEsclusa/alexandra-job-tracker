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
   * Scrapes a job posting from the given URL.
   */
  @ShellMethod(
    key = {"scrape", "sc"},
    value = """
      Scrape a job posting URL.
      
      EXAMPLES
        - scrape -u https://linkedin.com/jobs/123""",
    group = "Scraping")
  public String scrape(@ShellOption(value = {"--url", "-u"}, help = "Job posting URL to scrape") final String url) {
    var result = client.execute("""
        mutation($url: String!) {
          scrapeJobPosting(url: $url) { id title company source }
        }
        """,
      Map.of("url", url));
    return "Scraped: " + result.get("data").get("scrapeJobPosting");
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
          jobPostings(source: $s) { id title company source url postedAt }
        }""",
      variables);
    return jqProcessor.process(result.get("data").get("jobPostings"), jq);
  }
}
