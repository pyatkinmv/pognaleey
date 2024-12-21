package ru.pyatkinmv.pognaleey.dto;

import lombok.Setter;

import java.util.List;

public record TravelRecommendationDetailedOptionDto(
        String title,
        Budget budget,
        String reasoning,
        String creativeDescription,
        String tips,
        List<String> whereToGo,
        String additionalConsideration,
        @Setter
        String imageUrl
) {
    public record Budget(int from, int to) {
    }
}
