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
import ru.pyatkinmv.pognaleey.dto.TravelRecommendationDetailedOptionDto;
import ru.pyatkinmv.pognaleey.dto.TravelRecommendationDetailedOptionListDto;
import ru.pyatkinmv.pognaleey.dto.TravelRecommendationQuickOptionDto;
import ru.pyatkinmv.pognaleey.mapper.TravelInquiryMapper;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;
import ru.pyatkinmv.pognaleey.repository.TravelRecommendationRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static ru.pyatkinmv.pognaleey.mapper.TravelInquiryMapper.OBJECT_MAPPER;

@Slf4j
@Service
@RequiredArgsConstructor
public class TravelRecommendationService {
    private final GptHttpClient gptHttpClient;
    private final ImagesSearchHttpClient imagesSearchHttpClient;
    private final TravelRecommendationRepository recommendationRepository;
    private final ExecutorService executorService;

    private static final String PROMPT_TEMPLATE = "Придумай мне ровно 3 варианта путешествий исходя из входных условий." +
            " Добавь каждому варианту 1) необходимый бюджет 2) почему вариант подходит и чем хорош 3) общее описание " +
            "варианта 4) рекомендации (как добираться, куда сходить, чем заняться и тд) 5) что нужно дополнительно " +
            "учесть (подводные камни).  Входные условия: %s";

    //У меня есть следующие варианты для путешествия: 1) Санкт-Петербург, Россия; романтические прогулки и культурные мероприятия 2)Париж, Франция; ужины в ресторанах и посещение музеев|Прага, Чехия; уютные кафе и исторические достопримечательности
//
//Придумай мне ровно 3 варианта путешествий исходя из входных условий. Добавь каждому варианту 1) необходимый бюджет 2) почему вариант подходит и чем хорош 3) общее описание варианта 4) рекомендации (как добираться, куда сходить, чем заняться и тд) 5) что нужно дополнительно учесть (подводные камни).  Входные условия: %s
    private static final String PROMPT_TEMPLATE_ENGLISH = "Come up with exactly 3 travel recommendations for me based on the" +
            " input conditions. Add to each option 1) the necessary budget " +
            "2) why the option is suitable and what is good 3) a general description of the option " +
            "4) recommendations (how to get there, where to go, what to do, etc.) 5) what needs to " +
            "be taken into account additionally (pitfalls). Give the answer in HTML format. Input conditions: %s";

    private static final String QUICK_REC_TEMPLATE_ENGLISH = "Come up with exactly 3 travel recommendations for me based on the input " +
            "conditions. Give the answers in the format: " +
            "place 1;short description (up to 5 words)|place 2;short description|place 3;short description. " +
            "There is no need for any additional numbering and words, the answer is just one line. Conditions:%s";

    private static final String QUICK_REC_TEMPLATE_RUSSIAN = "Придумай мне ровно 3 варианта путешествий исходя из входных условий. Ответ выдай в формате: место1;описание|место2;описание|место3;описание. Описание не должно содержать более 5 слов. Пример: Париж;город любви и романтики|Рим;вечный город с богатой историей|Барселона;город, где слились история и современность. Не надо никаких дополнительных нумераций и слов, ответ просто одной строкой на русском языке. Условия: %s";

    private static final String DETAILED_REC_TEMPLATE_RUSSIAN = "У меня есть следующие %d варианта для путешествия: %s. Мои пожелания для путешествия следующие: %s. Дай мне исходя из этих предпочтений подробное описание этих вариантов в формате JSON (не надо никаких дополнительных нумераций и слов, в ответе только JSON). Формат: {recommendations: {title: НАЗВАНИЕ МЕСТА, budget: {from: БЮДЖЕТ ОТ, to: БЮДЖЕТ ДО}, reasoning: ПОЧЕМУ ЭТОТ ВАРИАНТ ПОДХОДИТ, creativeDescription: КРЕАТИВНОЕ ХУДОЖЕСТВЕННОЕ ОПИСАНИЕ ВАРИАНТА, tips: ОБЩИЕ РЕКОМЕНДАЦИИ, whereToGo: [],ДОСТОПРИМЕЧАТЕЛЬНОСТИ/КОНКРЕТНЫЕ МЕСТА РЕКОМЕНДУЕМЫЕ ДЛЯ ПОСЕЩЕНИЯ], additionalConsideration: ЧТО НУЖНО ДОПОЛНИТЕЛЬНО УЧЕСТЬ}}";

    public List<TravelRecommendation> createQuickRecommendations(Long inquiryId, String inquiryPayload) {
        var prompt = String.format(QUICK_REC_TEMPLATE_RUSSIAN, inquiryPayload);
        var answer = gptHttpClient.ask(prompt);
        var recommendations = parse(inquiryId, answer);
        recommendations = recommendationRepository.saveAll(recommendations);

        return StreamSupport.stream(recommendations.spliterator(), false).collect(Collectors.toList());
    }

    private static Iterable<TravelRecommendation> parse(Long inquiryId, String quickRecommendationsAnswer) {
        return toDtoQuickList(quickRecommendationsAnswer)
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
        var optionsStr = IntStream.range(0, recommendations.size())
                .mapToObj(i -> String.format("%d)%s—%s",
                        i + 1,
                        recommendations.get(i).getTitle(),
                        recommendations.get(i).getShortDescription()))
                .collect(Collectors.joining(";"));

        var prompt = String.format(DETAILED_REC_TEMPLATE_RUSSIAN, recommendations.size(), optionsStr, inquiryParams);
        var detailsRaw = gptHttpClient.ask(prompt);
        var parsed = toDtoDetailedList(detailsRaw).orElseThrow();

        if (recommendations.size() != parsed.recommendations().size()) {
            throw new IllegalArgumentException("Different size of recommendations");
        }

        var recIdToDetailsMap = IntStream.range(0, recommendations.size())
                .mapToObj(i -> Map.entry(recommendations.get(i).getId(), TravelInquiryMapper.toJson(parsed.recommendations().get(i))))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        log.info("recIdToDetailsMap before update: {}", recIdToDetailsMap);

        recIdToDetailsMap.forEach(recommendationRepository::updateDetails);

        log.info("end enrichWithDetailsAsync");
    }

    Collection<TravelRecommendation> findByInquiryId(Long inquiryId) {
        return recommendationRepository.findByInquiryId(inquiryId);
    }

    @SneakyThrows
    public static List<TravelRecommendationQuickOptionDto> toDtoQuickList(String recommendationPayload) {
        recommendationPayload = recommendationPayload.replaceAll("\"", "")
                .replaceAll("\\.", "");
        return Stream.of(recommendationPayload.split("\\|"))
                .map(it -> it.split(";"))
                .filter(TravelRecommendationService::isValid)
                .map(it -> new TravelRecommendationQuickOptionDto(-1L, it[0], it[1]))
                .toList();
    }

    @SneakyThrows
    public static Optional<TravelRecommendationDetailedOptionListDto> toDtoDetailedList(
            @Nullable String recommendationDetailedPayload
    ) {
        if (recommendationDetailedPayload != null) {
            return Optional.ofNullable(
                    OBJECT_MAPPER.readValue(
                            recommendationDetailedPayload,
                            TravelRecommendationDetailedOptionListDto.class
                    )
            );
        } else {
            return Optional.empty();
        }
    }

    @SneakyThrows
    public static Optional<TravelRecommendationDetailedOptionDto> toDtoDetailed(
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
                            TravelRecommendationDetailedOptionDto.class
                    )
            );
            return result;
        } else {
            return Optional.empty();
        }
    }

    private static boolean isValid(String[] it) {
        try {
            return !it[0].isEmpty() && !it[1].isEmpty();
        } catch (RuntimeException e) {
            log.error(e.getMessage());

            return false;
        }
    }

    @SneakyThrows
    @Async
    public void enrichWithImagesAsync(List<TravelRecommendation> recommendations) {
        log.info("begin enrichWithImagesAsync");

        // TODO
        var tasks = recommendations.stream().map(this::buildCallable).toList();
//        var futures = executorService.invokeAll(tasks);
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

    private record RecIdToImageUrl(long recId, String imageUrl) {
    }
}