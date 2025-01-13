package ru.pyatkinmv.pognaleey.dto;

import org.springframework.lang.Nullable;

import java.util.List;

public record TravelRecommendationDto(
        long id,
        String title,
        String budget,
        String reasoning,
        String creativeDescription,
        String tips,
        List<String> whereToGo,
        String additionalConsideration,
        String imageUrl,
        @Nullable Long guideId
) {
}
