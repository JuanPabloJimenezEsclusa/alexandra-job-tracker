package com.jobtracker.scraping.adapter;

import com.jobtracker.domain.port.out.JobScraperPort;
import com.jobtracker.domain.port.out.RawJobData;
import com.jobtracker.domain.vo.UserId;
import com.jobtracker.scraping.client.HumanizedHttpClient;
import org.springframework.stereotype.Component;

@Component
public class LinkedInScraperAdapter implements JobScraperPort {
  private final HumanizedHttpClient client;

  public LinkedInScraperAdapter(HumanizedHttpClient client) {
    this.client = client;
  }

  @Override
  public RawJobData scrape(UserId userId, String url) {
    var doc = client.fetch(url);
    var title = doc.select("h1.topcard__title").text();
    var company = doc.select("a.topcard__org-name-link").text();
    var description = doc.select(".description__text").text();
    if (title.isEmpty()) title = doc.title();
    return new RawJobData(url, title, company.isEmpty() ? "Unknown" : company, description, "LINKEDIN");
  }
}
