package ru.pyatkinmv.pognaleey.mapper;

import org.springframework.stereotype.Component;
import ru.pyatkinmv.pognaleey.dto.TravelInquiryDto;
import ru.pyatkinmv.pognaleey.dto.TravelRecommendationListDto;
import ru.pyatkinmv.pognaleey.dto.TravelShortRecommendationDto;
import ru.pyatkinmv.pognaleey.model.TravelInquiry;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;

import java.util.Collection;

import static ru.pyatkinmv.pognaleey.service.TravelRecommendationService.toDtoDetailed;

@Component
public class TravelMapper {

    public static TravelInquiryDto toDto(TravelInquiry inquiry, Collection<TravelRecommendation> recommendations) {
        return TravelInquiryDto.builder()
                .id(inquiry.getId())
                .payload(inquiry.getParams())
                .createdAt(inquiry.getCreatedAt())
                .quickRecommendations(recommendations.stream()
                        .map(it -> new TravelShortRecommendationDto(
                                it.getId(),
                                it.getTitle(),
                                it.getShortDescription())
                        ).toList())
                .build();
    }

    public static TravelRecommendationListDto toDto(Collection<TravelRecommendation> recommendations) {
        return new TravelRecommendationListDto(
                recommendations.stream()
                        .map(it -> toDtoDetailed(it.getDetails(), it.getImageUrl()).orElse(null))
                        .toList()
        );
    }

}
