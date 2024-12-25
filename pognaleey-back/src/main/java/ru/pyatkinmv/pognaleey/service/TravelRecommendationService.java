package ru.pyatkinmv.pognaleey.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.pyatkinmv.pognaleey.client.GptHttpClient;
import ru.pyatkinmv.pognaleey.client.ImagesSearchHttpClient;
import ru.pyatkinmv.pognaleey.dto.gpt.GptResponseQuickRecommendationListDto;
import ru.pyatkinmv.pognaleey.dto.gpt.GptResponseRecommendationDetailsListDto;
import ru.pyatkinmv.pognaleey.dto.gpt.HasRecommendations;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;
import ru.pyatkinmv.pognaleey.repository.TravelRecommendationRepository;
import ru.pyatkinmv.pognaleey.util.Utils;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class TravelRecommendationService {
    private final GptHttpClient gptHttpClient;
    private final ImagesSearchHttpClient imagesSearchHttpClient;
    private final TravelRecommendationRepository recommendationRepository;

    public List<TravelRecommendation> createShortRecommendations(Long inquiryId, String inquiryParams) {
        var prompt = PromptService.generateShortPrompt(3, inquiryParams);
        var answer = gptHttpClient.ask(prompt);
        var recommendations = parseQuick(inquiryId, answer);
        recommendations = recommendationRepository.saveAll(recommendations);

        return StreamSupport.stream(recommendations.spliterator(), false).collect(Collectors.toList());
    }

    private static Iterable<TravelRecommendation> parseQuick(Long inquiryId, String quickRecommendationsAnswer) {
        var parsed = parse(quickRecommendationsAnswer, GptResponseQuickRecommendationListDto.class);

        return parsed.recommendations()
                .stream()
                .map(it -> TravelRecommendation.builder()
                        .inquiryId(inquiryId)
                        .title(it.title())
                        .shortDescription(it.description())
                        .createdAt(Instant.now())
                        .build())
                .toList();
    }

    @SneakyThrows
    @Async
    public void enrichWithDetailsAsync(List<TravelRecommendation> recommendations, String inquiryParams) {
        log.info("begin enrichWithDetailsAsync");
        var prompt = PromptService.generateDetailedPrompt(recommendations, inquiryParams);
        var recommendationDetailsRaw = gptHttpClient.ask(prompt);
        var parsed = parse(recommendationDetailsRaw, GptResponseRecommendationDetailsListDto.class);

        if (recommendations.size() != parsed.recommendations().size()) {
            throw new IllegalArgumentException("Different size of recommendations");
        }

        var recommendationIdToDetailsMap = IntStream.range(0, recommendations.size())
                .mapToObj(i -> Map.entry(recommendations.get(i).getId(), Utils.toJson(parsed.recommendations().get(i))))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        log.info("recommendationIdToDetailsMap before update: {}", recommendationIdToDetailsMap);

        recommendationIdToDetailsMap.forEach(recommendationRepository::updateDetails);

        log.info("end enrichWithDetailsAsync");
    }

    @SneakyThrows
    static <T extends HasRecommendations<?>> T parse(String gptRecommendationsAnswerRaw, Class<T> clazz) {
        var regex = "\\{[\\s\\S]*\"recommendations\":[\\s\\S]*\\}";
        var pattern = Pattern.compile(regex);
        var matcher = pattern.matcher(gptRecommendationsAnswerRaw);

        if (matcher.find()) {
            var jsonStr = matcher.group(0);

            return Utils.toObject(jsonStr, clazz);
        } else {
            throw new RuntimeException("gptRecommendationsAnswerRaw has wrong format: " + gptRecommendationsAnswerRaw);
        }
    }

    Collection<TravelRecommendation> findByInquiryId(Long inquiryId) {
        return recommendationRepository.findByInquiryId(inquiryId);
    }

    // TODO: fixme
    @Deprecated
    @SneakyThrows
    @Async
    public void enrichWithImagesAsync(List<TravelRecommendation> recommendations) {
        log.info("begin enrichWithImagesAsync");

        // TODO: fixme
        //  Current api doesn't allow making more than one request per second
        var tasks = recommendations.stream().map(this::buildCallable).toList();
        var recIdsToImages = tasks.stream().map(it -> {
            try {
                RecIdToImageUrl call = it.call();
                Thread.sleep(1000);
                return call;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).toList();

        log.info("update recIdsToImages {}", recIdsToImages);

        recIdsToImages.forEach(it -> recommendationRepository.updateImageUrl(it.recId(), it.imageUrl()));

        log.info("end enrichWithImagesAsync");
    }

    private Callable<RecIdToImageUrl> buildCallable(TravelRecommendation recommendation) {
        return () -> new RecIdToImageUrl(
                recommendation.getId(),
                imagesSearchHttpClient.searchImageUrl(recToSearchText(recommendation))
        );
    }

    private static String recToSearchText(TravelRecommendation recommendation) {
        return String.format("%s-%s", recommendation.getTitle(), recommendation.getShortDescription());
    }

    public List<TravelRecommendation> findAllByIds(List<Long> recommendationIds) {
        return recommendationRepository.findAllByIdIn(recommendationIds);
    }

    private record RecIdToImageUrl(long recId, String imageUrl) {
    }
}