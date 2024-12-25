package ru.pyatkinmv.pognaleey.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.pyatkinmv.pognaleey.dto.TravelInquiryDto;
import ru.pyatkinmv.pognaleey.dto.TravelRecommendationListDto;
import ru.pyatkinmv.pognaleey.mapper.TravelMapper;
import ru.pyatkinmv.pognaleey.model.TravelInquiry;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;
import ru.pyatkinmv.pognaleey.repository.TravelInquiryRepository;

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

    public TravelInquiryDto createInquiry(Map<String, Object> inquiryParams) {
        var inquiryPayload = toString(inquiryParams);
        var inquiry = TravelInquiry.builder()
                .params(inquiryPayload)
                .createdAt(Instant.now())
                .build();
        inquiry = inquiryRepository.save(inquiry);
        List<TravelRecommendation> recommendations;

        try {
            recommendations = recommendationService.createShortRecommendations(inquiry.getId(), inquiryPayload);
            recommendationService.enrichWithDetailsAsync(recommendations, inquiryPayload);
            recommendationService.enrichWithImagesAsync(recommendations);

            return TravelMapper.toInquiryDto(inquiry, recommendations);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process inquiry", e);
        }
    }

    public TravelRecommendationListDto getInquiryRecommendations(Long inquiryId, long timeoutMillis) {
        var inquiry = inquiryRepository.findById(inquiryId);

        if (inquiry.isEmpty()) {
            throw new RuntimeException("Inquiry with id " + inquiryId + " does not exist");
        }

        var future = buildRecommendationFutureFetch(inquiryId, 500);
        Collection<TravelRecommendation> recommendations;

        try {
            recommendations = future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Timeout: Detailed recommendation not available yet.", e);
        }

        return TravelMapper.toRecommendationListDto(recommendations);
    }

    private CompletableFuture<Collection<TravelRecommendation>> buildRecommendationFutureFetch(Long inquiryId,
                                                                                               long sleepMs) {
        return CompletableFuture.supplyAsync(() -> {
            while (true) {
                var recommendations = recommendationService.findByInquiryId(inquiryId);

                if (!recommendations.isEmpty()
                        && recommendations.stream().allMatch(
                        it -> it.getDetails() != null && it.getImageUrl() != null)) {
                    return recommendations;
                }

                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private static String toString(Map<String, Object> params) {
        return filterNonEmpty(params).entrySet().stream()
                .map(it -> it.getKey() + "=" + it.getValue())
                .collect(Collectors.joining(";"));
    }

    //TODO: fixme
    @Deprecated
    private static Map<String, Object> filterNonEmpty(Map<String, Object> params) {
        return params.entrySet().stream()
                .filter(it -> {
                    if (it.getValue() instanceof String && ((String) it.getValue()).isEmpty()) {
                        return false;
                    }

                    if (it.getValue() instanceof Collection && ((Collection) it.getValue()).isEmpty()) {
                        return false;
                    }

                    if (it.getValue() instanceof Map && ((Map) it.getValue()).isEmpty()) {
                        return false;
                    }


                    return true;
                }).collect(Collectors.toMap(it -> it.getKey(), it -> it.getValue()));
    }

}

