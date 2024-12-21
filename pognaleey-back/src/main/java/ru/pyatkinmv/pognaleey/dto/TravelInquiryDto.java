package ru.pyatkinmv.pognaleey.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelInquiryDto {
    private Long id;
    private String payload;
    private Instant createdAt;
    private List<TravelRecommendationQuickOptionDto> quickRecommendations;
    private List<TravelRecommendationDto> detailedRecommendations;
}

