package ru.pyatkinmv.pognaleey.dto.gpt;

import java.util.List;

public record GptResponseQuickRecommendationListDto(List<GptResponseQuickRecommendationDto> recommendations)
        implements HasRecommendations<GptResponseQuickRecommendationListDto.GptResponseQuickRecommendationDto> {
    public record GptResponseQuickRecommendationDto(String title, String description) {

    }
}
