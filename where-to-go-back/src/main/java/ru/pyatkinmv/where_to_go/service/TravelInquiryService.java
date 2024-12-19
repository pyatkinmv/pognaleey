package ru.pyatkinmv.where_to_go.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ru.pyatkinmv.where_to_go.dto.TravelInquiryDto;
import ru.pyatkinmv.where_to_go.mapper.TravelInquiryMapper;
import ru.pyatkinmv.where_to_go.model.TravelInquiry;
import ru.pyatkinmv.where_to_go.model.TravelRecommendation;
import ru.pyatkinmv.where_to_go.repository.TravelInquiryRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TravelInquiryService {
    private final TravelInquiryRepository inquiryRepository;
    private final TravelRecommendationService recommendationService;

    public TravelInquiryDto createInquiry(Map<String, String> inquiryParams) {
        var inquiryPayload = toString(inquiryParams);
        var inquiry = TravelInquiry.builder()
                .params(inquiryPayload)
                .createdAt(Instant.now())
                .build();
        inquiry = inquiryRepository.save(inquiry);
        List<TravelRecommendation> recommendations;

        try {
            recommendations = recommendationService.createQuickRecommendations(inquiry.getId(), inquiryPayload);
//            recommendationService.enrichWithDetailsAsync(
//                    recommendations,
//                    inquiryPayload
//            );

            recommendationService.enrichWithImagesAsync(recommendations);

            return TravelInquiryMapper.toDto(inquiry, recommendations);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process inquiry", e);
        }
    }

    @SneakyThrows
    public TravelInquiryDto getInquiryWithDetailedRecommendation(Long inquiryId, long timeoutMillis) {
        if (!inquiryRepository.existsById(inquiryId)) {
            throw new RuntimeException("Inquiry with id " + inquiryId + " does not exist");
        }

        CompletableFuture<Collection<TravelRecommendation>> future = CompletableFuture.supplyAsync(() -> {
            while (true) {
                var recommendations = recommendationService.findByInquiryId(inquiryId);

                if (!recommendations.isEmpty()
                        && recommendations.stream().allMatch(it -> it.getDetails() != null)) {
                    return recommendations;
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Collection<TravelRecommendation> recommendations;
        try {
            recommendations = future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Timeout: Detailed recommendation not available yet.", e);
        }

        var inquiry = inquiryRepository.findById(inquiryId).orElseThrow();

        return TravelInquiryMapper.toDto(inquiry, recommendations);
    }

    private static String toString(Map<String, String> params) {
        return params.entrySet().stream()
                .map(it -> it.getKey() + "=" + it.getValue())
                .collect(Collectors.joining(";"));
    }

}

