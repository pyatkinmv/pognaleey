package ru.pyatkinmv.pognaleey.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.pyatkinmv.pognaleey.client.GptHttpClient;
import ru.pyatkinmv.pognaleey.client.ImagesSearchHttpClient;
import ru.pyatkinmv.pognaleey.dto.GptResponseRecommendationDetailsDto;
import ru.pyatkinmv.pognaleey.dto.TravelRecommendationListDto;
import ru.pyatkinmv.pognaleey.mapper.TravelMapper;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;
import ru.pyatkinmv.pognaleey.model.TravelRecommendationStatus;
import ru.pyatkinmv.pognaleey.repository.TravelGuideRepository;
import ru.pyatkinmv.pognaleey.repository.TravelInquiryRepository;
import ru.pyatkinmv.pognaleey.repository.TravelRecommendationRepository;
import ru.pyatkinmv.pognaleey.util.Utils;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import static ru.pyatkinmv.pognaleey.service.GptAnswerResolveHelper.parseSearchableItems;
import static ru.pyatkinmv.pognaleey.service.GptAnswerResolveHelper.resolveInCaseGeneratedMoreOrLessThanExpected;

@Slf4j
@Service
@RequiredArgsConstructor
public class TravelRecommendationService {
    public static final int RECOMMENDATIONS_NUMBER = 5;

    private final GptHttpClient gptHttpClient;
    private final ImagesSearchHttpClient imagesSearchHttpClient;
    private final TravelRecommendationRepository recommendationRepository;
    private final TravelInquiryRepository inquiryRepository;
    private final TravelGuideRepository guideRepository;
    private final ExecutorService executorService;

    private static List<TravelRecommendation> parseQuick(Long inquiryId, String quickRecommendationsAnswer) {
        var parsed = parseSearchableItems(quickRecommendationsAnswer);

        return parsed.stream()
                .map(it -> TravelRecommendation.builder()
                        .inquiryId(inquiryId)
                        .title(it.title().trim())
                        .imageSearchPhrase(it.imageSearchPhrase().trim())
                        .createdAt(Instant.now())
                        .status(TravelRecommendationStatus.IN_PROGRESS)
                        .build())
                .toList();
    }

    @SneakyThrows
    static GptResponseRecommendationDetailsDto parseDetailed(String gptRecommendationsAnswerRaw) {
        var regex = "\\{[\\s\\S]*\"description\":[\\s\\S]*\\}";
        var pattern = Pattern.compile(regex);
        var matcher = pattern.matcher(gptRecommendationsAnswerRaw);

        if (matcher.find()) {
            var jsonStr = matcher.group(0);

            return Utils.toObject(jsonStr, GptResponseRecommendationDetailsDto.class);
        } else {
            throw new RuntimeException("gptRecommendationsAnswerRaw has wrong format: " + gptRecommendationsAnswerRaw);
        }
    }

    @Async
    public void createRecommendationsAsync(Long inquiryId, String inquiryParams) {
        log.info("begin createRecommendationsAsync for inquiryId {}", inquiryId);
        var recommendations = createQuickRecommendations(inquiryId, inquiryParams);
        enrichWithDetailsAsyncEach(recommendations, inquiryParams);
        enrichWithImagesAsyncAll(recommendations);
    }

    List<TravelRecommendation> createQuickRecommendations(Long inquiryId, String inquiryParams) {
        var prompt = PromptService.generateQuickPrompt(RECOMMENDATIONS_NUMBER, inquiryParams);
        var answer = gptHttpClient.ask(prompt);
        var recommendations = parseQuick(inquiryId, answer);
        recommendations = resolveInCaseGeneratedMoreOrLessThanExpected(recommendations);

        log.info("save recommendations {}", recommendations);
        return recommendationRepository.saveAllFromIterable(recommendations);
    }

    @SneakyThrows
    void enrichWithDetailsAsyncEach(List<TravelRecommendation> recommendations, String inquiryParams) {
        log.info("begin enrichWithDetailsAsyncEach for recommendations {}", recommendations);
        recommendations.forEach(
                it -> executorService.execute(() -> enrichWithDetails(it, inquiryParams))
        );
    }

    private void enrichWithDetails(TravelRecommendation recommendation, String inquiryParams) {
        log.info("begin enrichWithDetails for recommendation: {}", recommendation.getId());

        try {
            var prompt = PromptService.generateDetailedPrompt(recommendation, inquiryParams);
            var recommendationDetailsRaw = gptHttpClient.ask(prompt);
            var details = parseDetailed(recommendationDetailsRaw);
            var detailsJson = Utils.toJson(details);
            log.info("update recommendation {} with details {}", recommendation.getId(), detailsJson);
            recommendationRepository.updateDetailsAndStatus(recommendation.getId(), detailsJson);
        } catch (Exception e) {
            log.error("Can not enrichWithDetails for recommendation {}, set failed status; reason: ",
                    recommendation.getId(), e);
            recommendationRepository.setStatus(recommendation.getId(), TravelRecommendationStatus.FAILED.name());
        }

        log.info("end enrichWithDetails for recommendation: {}", recommendation.getId());
    }

    List<TravelRecommendation> findByInquiryId(Long inquiryId) {
        return recommendationRepository.findByInquiryId(inquiryId);
    }

    void enrichWithImagesAsyncAll(List<TravelRecommendation> recommendations) {
        log.info("begin enrichWithImagesAsyncAll for recommendations {}", recommendations);
        executorService.execute(() -> recommendations.forEach(this::searchAndSaveAndUpdateStatus));
    }

    @SneakyThrows
    private void searchAndSaveAndUpdateStatus(TravelRecommendation recommendation) {
        var searchText = recommendation.getImageSearchPhrase();
        var imageUrl = imagesSearchHttpClient.searchImageUrlWithRateLimiting(searchText);

        if (imageUrl.isPresent()) {
            log.info("Update imageUrl {} for recommendation {}", imageUrl, recommendation.getId());
            recommendationRepository.updateImageUrlAndStatus(recommendation.getId(), imageUrl.get());
        } else {
            log.error("Not found image url for recommendation {}, set failed status", recommendation.getId());
            recommendationRepository.setStatus(recommendation.getId(), TravelRecommendationStatus.FAILED.name());
        }
    }

    public TravelRecommendation findById(long recommendationId) {
        return recommendationRepository.findById(recommendationId).orElseThrow();
    }

    public TravelRecommendationListDto getRecommendations(List<Long> recommendationIds) {
        var recommendations = recommendationRepository.findAllByIdIn(recommendationIds);
        var recommendationsIds = recommendations.stream().map(TravelRecommendation::getId).toList();
        var recommendationIdToGuideIdMap = guideRepository.getRecommendationToGuideMap(recommendationsIds);

        return TravelMapper.toRecommendationListDto(recommendations, recommendationIdToGuideIdMap);
    }

    public TravelRecommendationListDto getRecommendations(long inquiryId) {
        var inquiry = inquiryRepository.findById(inquiryId);

        if (inquiry.isEmpty()) {
            throw new RuntimeException("Inquiry with id " + inquiryId + " does not exist");
        }

        var recommendations = findByInquiryId(inquiryId);

        if (recommendations.isEmpty()) {
            return TravelMapper.toRecommendationListDto(Collections.emptyList(), Collections.emptyMap());
        }

        var recommendationsIds = recommendations.stream().map(TravelRecommendation::getId).toList();
        var recommendationIdToGuideIdMap = guideRepository.getRecommendationToGuideMap(recommendationsIds);

        return TravelMapper.toRecommendationListDto(recommendations, recommendationIdToGuideIdMap);
    }

    private Optional<List<TravelRecommendation>> findRecommendationsFilteringDetailsAndImages(Long inquiryId) {
        var recommendations = findByInquiryId(inquiryId);

        if (!recommendations.isEmpty()
                && recommendations.stream().allMatch(
                it -> it.getDetails() != null && it.getImageUrl() != null)) {
            return Optional.of(recommendations);
        } else {
            return Optional.empty();
        }
    }

}