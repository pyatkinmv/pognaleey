package ru.pyatkinmv.pognaleey.dto;

import java.util.List;

public record TravelRecommendationDto(
        String title,
        String budget,
        String reasoning,
        String creativeDescription,
        String tips,
        List<String> whereToGo,
        String additionalConsideration,
        String imageUrl
) {
}
