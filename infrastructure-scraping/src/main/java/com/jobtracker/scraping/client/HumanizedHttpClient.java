package com.jobtracker.scraping.client;

import java.util.List;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * HTTP client with human-like request patterns.
 */
@Component
public class HumanizedHttpClient {
  private static final List<String> USER_AGENTS = List.of(
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.2 Safari/605.1.15",
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
  );
  private static final List<String> REFERRERS = List.of(
    "https://www.google.com/search?q=software+engineer+jobs",
    "https://www.google.com/search?q=jobs+near+me",
    "https://linkedin.com/"
  );
  private final Random random = new Random();
  private final long minDelayMs;
  private final long maxDelayMs;

  /**
   * Constructor.
   */
  public HumanizedHttpClient(
    @Value("${scraper.delay.min:1000}") long minDelayMs,
    @Value("${scraper.delay.max:3000}") long maxDelayMs
  ) {
    this.minDelayMs = minDelayMs;
    this.maxDelayMs = maxDelayMs;
  }

  /**
   * Fetches a URL with human-like delays and headers.
   */
  public Document fetch(String url) {
    try {
      Thread.sleep(minDelayMs + (random.nextLong() * (maxDelayMs - minDelayMs)));
      return Jsoup.connect(url)
        .userAgent(USER_AGENTS.get(random.nextInt(USER_AGENTS.size())))
        .referrer(REFERRERS.get(random.nextInt(REFERRERS.size())))
        .timeout(10000)
        .get();
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ScrapingException("Interrupted while waiting for request", e);
    } catch (final Exception e) {
      throw new ScrapingException("Failed to fetch " + url, e);
    }
  }
}
