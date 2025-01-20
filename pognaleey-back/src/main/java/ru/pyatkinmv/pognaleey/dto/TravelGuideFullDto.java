package ru.pyatkinmv.pognaleey.dto;

import jakarta.annotation.Nullable;

public record TravelGuideFullDto(long id, String title, String imageUrl, String details, int totalLikes,
                                 boolean isLiked, long createdAt, @Nullable UserDto owner) {
}
