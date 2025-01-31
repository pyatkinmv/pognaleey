package ru.pyatkinmv.pognaleey.dto;

import org.springframework.lang.Nullable;

public record TravelRecommendationDto(
    long id,
    String title,
    String status,
    @Nullable DetailsDto details,
    @Nullable ImageDto image,
    @Nullable Long guideId) {
  public record DetailsDto(String description, String reasoning) {}
}
