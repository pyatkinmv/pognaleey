package ru.pyatkinmv.pognaleey.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.pyatkinmv.pognaleey.dto.TravelInquiryDto;
import ru.pyatkinmv.pognaleey.mapper.TravelMapper;
import ru.pyatkinmv.pognaleey.model.TravelInquiry;
import ru.pyatkinmv.pognaleey.model.User;
import ru.pyatkinmv.pognaleey.repository.TravelInquiryRepository;
import ru.pyatkinmv.pognaleey.security.AuthenticatedUserProvider;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
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
        var userId = AuthenticatedUserProvider.getCurrentUser().map(User::getId).orElse(null);
        var inquiry = TravelInquiry.builder()
                .params(inquiryPayload)
                .createdAt(Instant.now())
                .userId(userId)
                .build();
        inquiry = inquiryRepository.save(inquiry);
        var recommendations = recommendationService.createBlueprintRecommendations(inquiry.getId());
        recommendationService.enrichRecommendationsAsync(recommendations, inquiryPayload);

        return TravelMapper.toInquiryDto(inquiry);
    }

    public TravelInquiry findById(Long inquiryId) {
        return inquiryRepository.findById(inquiryId).orElseThrow();
    }
}

