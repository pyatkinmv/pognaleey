package ru.pyatkinmv.pognaleey.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.pyatkinmv.pognaleey.dto.TravelInquiryDto;
import ru.pyatkinmv.pognaleey.dto.TravelRecommendationListDto;
import ru.pyatkinmv.pognaleey.mapper.TravelMapper;
import ru.pyatkinmv.pognaleey.model.TravelInquiry;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;
import ru.pyatkinmv.pognaleey.repository.TravelInquiryRepository;
import ru.pyatkinmv.pognaleey.util.LongPolling;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TravelInquiryService {
    private final TravelInquiryRepository inquiryRepository;
    private final TravelRecommendationService recommendationService;

    private static String toStringFilteringNonEmpty(Map<String, Object> params) {
        var filteredMap = params.entrySet()
                .stream()
                .filter(entry -> !isEmpty(entry.getValue())) // Удаляем пустые значения
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return filteredMap.entrySet().stream()
                .map(it -> it.getKey() + "=" + it.getValue())
                .collect(Collectors.joining(";"));
    }

    private static boolean isEmpty(Object it) {
        if (it instanceof String && ((String) it).isEmpty()) {
            return true;
        }

        if (it instanceof Collection && ((Collection<?>) it).isEmpty()) {
            return true;
        }

        if (it instanceof Map && ((Map<?, ?>) it).isEmpty()) {
            return true;
        }

        return false;
    }

    public TravelInquiryDto createInquiry(Map<String, Object> inquiryParams) {
        var inquiryPayload = toStringFilteringNonEmpty(inquiryParams);
        var inquiry = TravelInquiry.builder()
                .params(inquiryPayload)
                .createdAt(Instant.now())
                .build();
        inquiry = inquiryRepository.save(inquiry);
        List<TravelRecommendation> recommendations;

        try {
            recommendations = recommendationService.createQuickRecommendations(inquiry.getId(), inquiryPayload);
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

        var longPoll = new LongPolling<List<TravelRecommendation>>();
        var recommendations = longPoll.execute(
                () -> findRecommendationsFilteringDetailsAndImages(inquiryId),
                timeoutMillis,
                300
        );

        return TravelMapper.toRecommendationListDto(recommendations);
    }

    private Optional<List<TravelRecommendation>> findRecommendationsFilteringDetailsAndImages(Long inquiryId) {
        var recommendations = recommendationService.findByInquiryId(inquiryId);

        if (!recommendations.isEmpty()
                && recommendations.stream().allMatch(
                it -> it.getDetails() != null && it.getImageUrl() != null)) {
            return Optional.of(recommendations);
        } else {
            return Optional.empty();
        }
    }

}

