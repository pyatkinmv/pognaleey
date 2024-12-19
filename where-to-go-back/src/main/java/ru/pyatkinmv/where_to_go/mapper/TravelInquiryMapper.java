package ru.pyatkinmv.where_to_go.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import ru.pyatkinmv.where_to_go.dto.TravelInquiryDto;
import ru.pyatkinmv.where_to_go.dto.TravelRecommendationDetailedOptionDto;
import ru.pyatkinmv.where_to_go.dto.TravelRecommendationDetailedOptionListDto;
import ru.pyatkinmv.where_to_go.dto.TravelRecommendationQuickOptionDto;
import ru.pyatkinmv.where_to_go.model.TravelInquiry;
import ru.pyatkinmv.where_to_go.model.TravelRecommendation;

import java.util.Collection;

import static ru.pyatkinmv.where_to_go.service.TravelRecommendationService.*;

@Component
public class TravelInquiryMapper {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static TravelInquiryDto toDto(TravelInquiry inquiry, Collection<TravelRecommendation> recommendations) {
        return TravelInquiryDto.builder()
                .id(inquiry.getId())
                .payload(inquiry.getParams())
                .createdAt(inquiry.getCreatedAt())
                .quickOptions(recommendations.stream()
                        .map(it -> new TravelRecommendationQuickOptionDto(
                                it.getTitle(),
                                it.getShortDescription())
                        ).toList())
                .detailedOptions(recommendations.stream()
                        .map(it -> toDtoDetailed(it.getDetails(), it.getImageUrl()).orElse(null)).toList())
                .build();
    }

    @SneakyThrows
    public static String toJson(Object obj) {
        return OBJECT_MAPPER.writeValueAsString(obj);
    }
}
