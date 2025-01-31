package ru.pyatkinmv.pognaleey.dto;

import org.springframework.lang.Nullable;

// NOTE: Maintain backward compatibility
// TODO: implement test
public record ImageDto(
    @Nullable Long id,
    String title,
    @Nullable String url,
    @Nullable String thumbnailUrl,
    String query,
    Boolean aiGenerated,
    @Nullable String licenceUrl,
    @Nullable String authorName,
    @Nullable String authorUrl) {
  public ImageDto(String url, String title, String thumbnailUrl, String query) {
    this(null, url, title, thumbnailUrl, query, false, null, null, null);
  }

  public static ImageDto withoutUrls(String title, String query) {
    return new ImageDto(null, title, null, null, query, false, null, null, null);
  }

  public boolean isWithoutUrls() {
    return !isWithUrls();
  }

  public boolean isWithUrls() {
    return url != null && thumbnailUrl != null;
  }
}
