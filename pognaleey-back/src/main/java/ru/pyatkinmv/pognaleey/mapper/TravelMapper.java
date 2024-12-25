package ru.pyatkinmv.pognaleey.mapper;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import ru.pyatkinmv.pognaleey.dto.TravelInquiryDto;
import ru.pyatkinmv.pognaleey.dto.TravelQuickRecommendationDto;
import ru.pyatkinmv.pognaleey.dto.TravelRecommendationDto;
import ru.pyatkinmv.pognaleey.dto.TravelRecommendationListDto;
import ru.pyatkinmv.pognaleey.dto.gpt.GptResponseRecommendationDetailsListDto;
import ru.pyatkinmv.pognaleey.model.TravelInquiry;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;
import ru.pyatkinmv.pognaleey.util.Utils;

import java.util.Collection;

@Component
public class TravelMapper {

    public static TravelInquiryDto toInquiryDto(TravelInquiry inquiry, Collection<TravelRecommendation> recommendations) {
        return TravelInquiryDto.builder()
                .id(inquiry.getId())
                .payload(inquiry.getParams())
                .createdAt(inquiry.getCreatedAt())
                .quickRecommendations(recommendations.stream()
                        .map(it -> new TravelQuickRecommendationDto(
                                it.getId(),
                                it.getTitle(),
                                it.getShortDescription())
                        ).toList())
                .build();
    }

    public static TravelRecommendationListDto toRecommendationListDto(Collection<TravelRecommendation> recommendations) {
        return new TravelRecommendationListDto(
                recommendations.stream()
                        .map(it -> toRecommendationDto(it.getDetails(), it.getImageUrl()))
                        .toList()
        );
    }

    @SneakyThrows
    public static TravelRecommendationDto toRecommendationDto(String recommendationDetailsJson, String imageUrl) {
        var details = Utils.toObject(
                recommendationDetailsJson,
                GptResponseRecommendationDetailsListDto.GptResponseRecommendationDetailsDto.class
        );

        return new TravelRecommendationDto(
                details.title(),
                new TravelRecommendationDto.Budget(
                        Utils.parseInt(details.budget().from()),
                        Utils.parseInt(details.budget().to())
                ),
                details.reasoning(),
                details.creativeDescription(),
                details.tips(),
                details.whereToGo(),
                details.additionalConsideration(),
                imageUrl
        );
    }
}
