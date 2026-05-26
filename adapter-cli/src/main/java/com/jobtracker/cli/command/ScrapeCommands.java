package com.jobtracker.cli.command;

import java.util.Map;

import com.jobtracker.cli.client.GraphqlClient;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class ScrapeCommands {
  private final GraphqlClient client;

  public ScrapeCommands(final GraphqlClient client) {
    this.client = client;
  }

  @ShellMethod(value = "Scrape a job posting URL", group = "Scraping")
  public String scrape(final String url) {
    var result = client.execute("""
        mutation($url: String!) {
          scrapeJobPosting(url: $url) { id title company source }
        }
        """,
      Map.of("url", url));
    return "Scraped: " + result.get("data").get("scrapeJobPosting");
  }
}
