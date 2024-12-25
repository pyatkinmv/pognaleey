package ru.pyatkinmv.pognaleey.dto.gpt;

import java.util.List;

public record GptResponseRecommendationDetailsListDto(List<GptResponseRecommendationDetailsDto> recommendations)
        implements HasRecommendations<GptResponseRecommendationDetailsListDto.GptResponseRecommendationDetailsDto> {
    public record GptResponseRecommendationDetailsDto(String title, Budget budget, String reasoning,
                                                      String creativeDescription, String tips, List<String> whereToGo,
                                                      String additionalConsideration) {

    }

    public record Budget(String from, String to) {
    }
}