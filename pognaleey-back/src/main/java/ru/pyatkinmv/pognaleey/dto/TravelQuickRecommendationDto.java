package ru.pyatkinmv.pognaleey.dto;

public record TravelQuickRecommendationDto(Long id, String title,
                                           // TODO: it's not description any longer; it's imageSearchPhrase
                                           String description) {
}