package ru.pyatkinmv.where_to_go.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;
import ru.pyatkinmv.where_to_go.dto.TravelInquiryDto;
import ru.pyatkinmv.where_to_go.dto.TravelRecommendationDetailedOptionListDto;
import ru.pyatkinmv.where_to_go.model.TravelInquiry;
import ru.pyatkinmv.where_to_go.model.TravelRecommendation;

import static ru.pyatkinmv.where_to_go.service.TravelRecommendationService.toDtoDetailedList;
import static ru.pyatkinmv.where_to_go.service.TravelRecommendationService.toDtoQuickList;

@Component
public class TravelInquiryMapper {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static TravelInquiryDto toDto(TravelInquiry inquiry, @Nullable TravelRecommendation recommendation) {
        return TravelInquiryDto.builder()
                .id(inquiry.getId())
                .payload(inquiry.getPayload())
                .createdAt(inquiry.getCreatedAt())
                .quickOptions(recommendation != null ? toDtoQuickList(recommendation.getQuickPayload()) : null)
                .detailedOptions(recommendation != null
                        ? toDtoDetailedList(recommendation.getDetailedPayload())
                        .map(TravelRecommendationDetailedOptionListDto::options)
                        .orElse(null)
                        : null)
                .build();
    }
}
