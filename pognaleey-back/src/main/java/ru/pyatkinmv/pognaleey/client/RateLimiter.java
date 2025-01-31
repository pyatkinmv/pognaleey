package ru.pyatkinmv.pognaleey.client;

@SuppressWarnings("UnstableApiUsage")
public class RateLimiter {
  private final com.google.common.util.concurrent.RateLimiter rateLimiter;

  public RateLimiter(double permitsPerSecond) {
    this.rateLimiter = com.google.common.util.concurrent.RateLimiter.create(permitsPerSecond);
  }

  public void acquire() {
    rateLimiter.acquire();
  }
}
