package ru.pyatkinmv.pognaleey.dto;

import org.springframework.lang.Nullable;

public record TravelRecommendationDto(
        long id,
        String title,
        String reasoning,
        String description,
        String imageUrl,
        @Nullable Long guideId
) {
}
