package ru.pyatkinmv.pognaleey.dto.gpt;

import java.util.List;

public record GptResponseRecommendationDetailsDto(String title, String budget, String reasoning,
                                                  String creativeDescription, String tips, List<String> whereToGo,
                                                  String additionalConsideration) {
}