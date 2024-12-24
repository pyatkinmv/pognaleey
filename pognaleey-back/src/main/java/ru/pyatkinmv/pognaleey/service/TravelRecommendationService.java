package ru.pyatkinmv.pognaleey.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.pyatkinmv.pognaleey.client.GptHttpClient;
import ru.pyatkinmv.pognaleey.client.ImagesSearchHttpClient;
import ru.pyatkinmv.pognaleey.dto.TravelRecommendationDto;
import ru.pyatkinmv.pognaleey.dto.TravelRecommendationListDto;
import ru.pyatkinmv.pognaleey.dto.TravelShortRecommendationDto;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;
import ru.pyatkinmv.pognaleey.repository.TravelRecommendationRepository;
import ru.pyatkinmv.pognaleey.util.Utils;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static ru.pyatkinmv.pognaleey.util.Utils.OBJECT_MAPPER;

@Slf4j
@Service
@RequiredArgsConstructor
public class TravelRecommendationService {
    private final GptHttpClient gptHttpClient;
    private final ImagesSearchHttpClient imagesSearchHttpClient;
    private final TravelRecommendationRepository recommendationRepository;

    public List<TravelRecommendation> createShortRecommendations(Long inquiryId, String inquiryParams) {
        var prompt = PromptService.getShortPrompt(3, inquiryParams);
        var answer = gptHttpClient.ask(prompt);
        var recommendations = parse(inquiryId, answer);
        recommendations = recommendationRepository.saveAll(recommendations);

        return StreamSupport.stream(recommendations.spliterator(), false).collect(Collectors.toList());
    }

    private static Iterable<TravelRecommendation> parse(Long inquiryId, String shortRecommendationsAnswer) {
        return toDtoQuickList(shortRecommendationsAnswer)
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
        var prompt = PromptService.getDetailedPrompt(recommendations, inquiryParams);
        var detailsRaw = gptHttpClient.ask(prompt);
        var parsed = toDtoDetailedList(detailsRaw).orElseThrow();

        if (recommendations.size() != parsed.recommendations().size()) {
            throw new IllegalArgumentException("Different size of recommendations");
        }

        var recIdToDetailsMap = IntStream.range(0, recommendations.size())
                .mapToObj(i -> Map.entry(recommendations.get(i).getId(), Utils.toJson(parsed.recommendations().get(i))))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        log.info("recIdToDetailsMap before update: {}", recIdToDetailsMap);

        recIdToDetailsMap.forEach(recommendationRepository::updateDetails);

        log.info("end enrichWithDetailsAsync");
    }

    Collection<TravelRecommendation> findByInquiryId(Long inquiryId) {
        return recommendationRepository.findByInquiryId(inquiryId);
    }

    // TODO: fixme
    @Deprecated
    @SneakyThrows
    public static List<TravelShortRecommendationDto> toDtoQuickList(String recommendationPayload) {
        recommendationPayload = recommendationPayload.replaceAll("\"", "")
                .replaceAll("\\.", "");
        return Stream.of(recommendationPayload.split("\\|"))
                .map(it -> it.split(";"))
                .filter(TravelRecommendationService::isValid)
                .map(it -> new TravelShortRecommendationDto(-1L, it[0], it[1]))
                .toList();
    }

    // TODO: fixme
    @Deprecated
    @SneakyThrows
    public static Optional<TravelRecommendationListDto> toDtoDetailedList(
            @Nullable String recommendationDetailedPayload
    ) {
        if (recommendationDetailedPayload != null) {
            return Optional.ofNullable(
                    OBJECT_MAPPER.readValue(
                            recommendationDetailedPayload,
                            TravelRecommendationListDto.class
                    )
            );
        } else {
            return Optional.empty();
        }
    }

    // TODO: fixme
    @Deprecated
    @SneakyThrows
    public static Optional<TravelRecommendationDto> toDtoDetailed(
            @Nullable String recommendationDetailedPayload,
            @Nullable String imageUrl
    ) {
        if (recommendationDetailedPayload != null) {
            var json = (ObjectNode) OBJECT_MAPPER.readTree(recommendationDetailedPayload);
            json.put("imageUrl", imageUrl);
            recommendationDetailedPayload = OBJECT_MAPPER.writeValueAsString(json);

            var result = Optional.ofNullable(
                    OBJECT_MAPPER.readValue(
                            recommendationDetailedPayload,
                            TravelRecommendationDto.class
                    )
            );
            return result;
        } else {
            return Optional.empty();
        }
    }

    // TODO: fixme
    @Deprecated
    private static boolean isValid(String[] it) {
        try {
            return !it[0].isEmpty() && !it[1].isEmpty();
        } catch (RuntimeException e) {
            log.error(e.getMessage());

            return false;
        }
    }

    // TODO: fixme
    @Deprecated
    @SneakyThrows
    @Async
    public void enrichWithImagesAsync(List<TravelRecommendation> recommendations) {
        log.info("begin enrichWithImagesAsync");

        // TODO
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