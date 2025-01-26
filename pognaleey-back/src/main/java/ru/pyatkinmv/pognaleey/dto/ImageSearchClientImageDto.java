package ru.pyatkinmv.pognaleey.dto;

import org.springframework.lang.Nullable;

public record ImageSearchClientImageDto(String url, String thumbnailUrl, String query, @Nullable String licenceUrl,
                                        @Nullable String authorName, @Nullable String authorUrl) {
    public ImageSearchClientImageDto(String url, String thumbnailUrl, String query) {
        this(url, thumbnailUrl, query, null, null, null);
    }
}
