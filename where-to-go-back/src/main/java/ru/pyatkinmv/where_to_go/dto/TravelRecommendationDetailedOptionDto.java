package ru.pyatkinmv.where_to_go.dto;

import lombok.Setter;

import java.util.List;

public record TravelRecommendationDetailedOptionDto(
        String placeName,
        String budget,
        String reasoning,
        String creativeDescription,
        String tips,
        List<String> whereToGo,
        String additionalConsideration,
        @Setter
        String imageUrl
) {
}