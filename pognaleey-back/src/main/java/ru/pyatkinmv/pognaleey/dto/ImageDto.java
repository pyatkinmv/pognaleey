package ru.pyatkinmv.pognaleey.dto;

import org.springframework.lang.Nullable;

public record ImageDto(@Nullable Long id, String title, String url, String thumbnailUrl, String query) {
    public ImageDto(String url, String title, String thumbnailUrl, String query) {
        this(null, url, title, thumbnailUrl, query);
    }
}