package ru.pyatkinmv.pognaleey.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import ru.pyatkinmv.pognaleey.dto.TravelInquiryDto;
import ru.pyatkinmv.pognaleey.dto.TravelRecommendationQuickOptionDto;
import ru.pyatkinmv.pognaleey.model.TravelInquiry;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;

import java.util.Collection;

import static ru.pyatkinmv.pognaleey.service.TravelRecommendationService.toDtoDetailed;

@Component
public class TravelInquiryMapper {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);


    public static TravelInquiryDto toDto(TravelInquiry inquiry, Collection<TravelRecommendation> recommendations) {
        return TravelInquiryDto.builder()
                .id(inquiry.getId())
                .payload(inquiry.getParams())
                .createdAt(inquiry.getCreatedAt())
                .quickRecommendations(recommendations.stream()
                        .map(it -> new TravelRecommendationQuickOptionDto(
                                it.getId(),
                                it.getTitle(),
                                it.getShortDescription())
                        ).toList())
                .detailedRecommendations(recommendations.stream()
                        .map(it -> toDtoDetailed(it.getDetails(), it.getImageUrl()).orElse(null)).toList())
                .build();
    }

    @SneakyThrows
    public static String toJson(Object obj) {
        return OBJECT_MAPPER.writeValueAsString(obj);
    }
}
