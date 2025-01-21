package ru.pyatkinmv.pognaleey.dto;

import jakarta.annotation.Nullable;

public record TravelGuideInfoDto(long id, @Nullable String title, @Nullable String imageUrl, int totalLikes,
                                 boolean isLiked, long createdAt, @Nullable UserDto owner) {
}
