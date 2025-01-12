package ru.pyatkinmv.pognaleey.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.pyatkinmv.pognaleey.client.GptHttpClient;
import ru.pyatkinmv.pognaleey.client.ImagesSearchHttpClient;
import ru.pyatkinmv.pognaleey.dto.GptResponseRecommendationDetailsDto;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;
import ru.pyatkinmv.pognaleey.repository.TravelRecommendationRepository;
import ru.pyatkinmv.pognaleey.util.Utils;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import static ru.pyatkinmv.pognaleey.service.GptAnswerResolveHelper.parseSearchableItems;
import static ru.pyatkinmv.pognaleey.service.GptAnswerResolveHelper.resolveInCaseGeneratedMoreOrLessThanExpected;

@Slf4j
@Service
@RequiredArgsConstructor
public class TravelRecommendationService {
    public static final int RECOMMENDATIONS_NUMBER = 3;

    private final GptHttpClient gptHttpClient;
    private final ImagesSearchHttpClient imagesSearchHttpClient;
    private final TravelRecommendationRepository recommendationRepository;
    private final ExecutorService executorService;

    private static List<TravelRecommendation> parseQuick(Long inquiryId, String quickRecommendationsAnswer) {
        var parsed = parseSearchableItems(quickRecommendationsAnswer);

        return parsed.stream()
                .map(it -> TravelRecommendation.builder()
                        .inquiryId(inquiryId)
                        .title(it.title().trim())
                        .shortDescription(it.imageSearchPhrase().trim())
                        .createdAt(Instant.now())
                        .build())
                .toList();
    }

    @SneakyThrows
    static GptResponseRecommendationDetailsDto parseDetailed(String gptRecommendationsAnswerRaw) {
        var regex = "\\{[\\s\\S]*\"title\":[\\s\\S]*\\}";
        var pattern = Pattern.compile(regex);
        var matcher = pattern.matcher(gptRecommendationsAnswerRaw);

        if (matcher.find()) {
            var jsonStr = matcher.group(0);

            return Utils.toObject(jsonStr, GptResponseRecommendationDetailsDto.class);
        } else {
            throw new RuntimeException("gptRecommendationsAnswerRaw has wrong format: " + gptRecommendationsAnswerRaw);
        }
    }

    public List<TravelRecommendation> createQuickRecommendations(Long inquiryId, String inquiryParams) {
        var prompt = PromptService.generateQuickPrompt(RECOMMENDATIONS_NUMBER, inquiryParams);
        var answer = gptHttpClient.ask(prompt);
        var recommendations = parseQuick(inquiryId, answer);
        recommendations = resolveInCaseGeneratedMoreOrLessThanExpected(recommendations);

        return recommendationRepository.saveAllFromIterable(recommendations);
    }

    private void enrichWithDetailsAsync(TravelRecommendation recommendation, String inquiryParams) {
        log.info("begin enrichWithDetailsAsync for recommendation: {}", recommendation.getId());
        var prompt = PromptService.generateDetailedPrompt(recommendation, inquiryParams);
        var recommendationDetailsRaw = gptHttpClient.ask(prompt);
        var details = parseDetailed(recommendationDetailsRaw);
        var detailsJson = Utils.toJson(details);
        log.info("update recommendation {} with details {}", recommendation.getId(), detailsJson);
        recommendationRepository.updateDetails(recommendation.getId(), detailsJson);
        log.info("end enrichWithDetailsAsync for recommendation: {}", recommendation.getId());
    }

    @SneakyThrows
    public void enrichWithDetailsAsync(List<TravelRecommendation> recommendations, String inquiryParams) {
        recommendations.forEach(
                it -> executorService.execute(() -> enrichWithDetailsAsync(it, inquiryParams))
        );
    }

    List<TravelRecommendation> findByInquiryId(Long inquiryId) {
        return recommendationRepository.findByInquiryId(inquiryId);
    }

    @Async
    public void enrichWithImagesAsync(List<TravelRecommendation> recommendations) {
        log.info("begin enrichWithImagesAsync");
        recommendations.forEach(this::searchAndSave);
        log.info("end enrichWithImagesAsync");
    }

    @SneakyThrows
    private void searchAndSave(TravelRecommendation recommendation) {
        var searchText = recommendation.getShortDescription();
        var imageUrl = imagesSearchHttpClient.searchImageUrlWithRateLimiting(searchText);
        log.info("Update imageUrl {} for recommendation {}", imageUrl, recommendation.getId());
        recommendationRepository.updateImageUrl(recommendation.getId(), imageUrl);
    }

    public List<TravelRecommendation> findAllByIds(List<Long> recommendationIds) {
        return recommendationRepository.findAllByIdIn(recommendationIds);
    }

    public TravelRecommendation findById(long recommendationId) {
        return recommendationRepository.findById(recommendationId).orElseThrow();
    }


}