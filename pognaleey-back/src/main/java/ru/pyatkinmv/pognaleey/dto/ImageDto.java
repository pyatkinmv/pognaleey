package ru.pyatkinmv.pognaleey.dto;

import org.springframework.lang.Nullable;

// NOTE: Maintain backward compatibility
// TODO: implement test
public record ImageDto(@Nullable Long id, String title, String url, String thumbnailUrl, String query,
                       Boolean aiGenerated, @Nullable String licenceUrl, @Nullable String authorName,
                       @Nullable String authorUrl) {
    public ImageDto(String url, String title, String thumbnailUrl, String query) {
        this(null, url, title, thumbnailUrl, query, false, null, null, null);
    }
}