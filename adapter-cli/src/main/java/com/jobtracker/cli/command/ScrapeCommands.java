package com.jobtracker.cli.command;

import java.util.HashMap;
import java.util.Map;

import com.jobtracker.cli.client.GraphqlClient;
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

  /**
   * Constructs scrape commands with the given GraphQL client.
   */
  public ScrapeCommands(final GraphqlClient client) {
    this.client = client;
  }

  /**
   * Scrapes a job posting from the given URL.
   */
  @ShellMethod(key = {"scrape", "sc"}, value = "Scrape a job posting URL", group = "Scraping")
  public String scrape(@ShellOption(value = {"--url", "-u"}) final String url) {
    var result = client.execute("""
        mutation($url: String!) {
          scrapeJobPosting(url: $url) { id title company source }
        }
        """,
      Map.of("url", url));
    return "Scraped: " + result.get("data").get("scrapeJobPosting");
  }

  /**
   * Lists scraped job postings, optionally filtered by source.
   */
  @ShellMethod(key = {"postings", "po"}, value = "List job postings", group = "Scraping")
  public String postings(
    @ShellOption(value = {"--source", "-s"}, defaultValue = ShellOption.NULL) @Nullable final String source) {
    final var variables = new HashMap<String, Object>();
    if (source != null) variables.put("s", source);
    final var result = client.execute("""
        query($s: Source) {
          jobPostings(source: $s) { id title company source url postedAt }
        }""",
      variables);
    return result.get("data").get("jobPostings").toPrettyString();
  }
}
