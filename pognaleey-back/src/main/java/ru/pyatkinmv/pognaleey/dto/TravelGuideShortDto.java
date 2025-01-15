package ru.pyatkinmv.pognaleey.dto;

import jakarta.annotation.Nullable;

public record TravelGuideShortDto(long id, String title, String imageUrl, int totalLikes, boolean isLiked,
                                  @Nullable UserDto owner) {
}
