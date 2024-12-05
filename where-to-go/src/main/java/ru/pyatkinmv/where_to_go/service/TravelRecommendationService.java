package ru.pyatkinmv.where_to_go.service;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.pyatkinmv.where_to_go.client.GptHttpClient;
import ru.pyatkinmv.where_to_go.dto.TravelRecommendationDetailedOptionListDto;
import ru.pyatkinmv.where_to_go.dto.TravelRecommendationQuickOptionDto;
import ru.pyatkinmv.where_to_go.model.TravelRecommendation;
import ru.pyatkinmv.where_to_go.repository.TravelRecommendationRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.pyatkinmv.where_to_go.mapper.TravelInquiryMapper.OBJECT_MAPPER;

@Slf4j
@Service
@RequiredArgsConstructor
public class TravelRecommendationService {
    private final GptHttpClient gptHttpClient;
    private final TravelRecommendationRepository recommendationRepository;

    private static final String PROMPT_TEMPLATE = "Придумай мне ровно 3 варианта путешествий исходя из входных условий." +
            " Добавь каждому варианту 1) необходимый бюджет 2) почему вариант подходит и чем хорош 3) общее описание " +
            "варианта 4) рекомендации (как добираться, куда сходить, чем заняться и тд) 5) что нужно дополнительно " +
            "учесть (подводные камни).  Входные условия: %s";

    //У меня есть следующие варианты для путешествия: 1) Санкт-Петербург, Россия; романтические прогулки и культурные мероприятия 2)Париж, Франция; ужины в ресторанах и посещение музеев|Прага, Чехия; уютные кафе и исторические достопримечательности
//
//Придумай мне ровно 3 варианта путешествий исходя из входных условий. Добавь каждому варианту 1) необходимый бюджет 2) почему вариант подходит и чем хорош 3) общее описание варианта 4) рекомендации (как добираться, куда сходить, чем заняться и тд) 5) что нужно дополнительно учесть (подводные камни).  Входные условия: %s
    private static final String PROMPT_TEMPLATE_ENGLISH = "Come up with exactly 3 travel options for me based on the" +
            " input conditions. Add to each option 1) the necessary budget " +
            "2) why the option is suitable and what is good 3) a general description of the option " +
            "4) recommendations (how to get there, where to go, what to do, etc.) 5) what needs to " +
            "be taken into account additionally (pitfalls). Give the answer in HTML format. Input conditions: %s";

    private static final String QUICK_REC_TEMPLATE_ENGLISH = "Come up with exactly 3 travel options for me based on the input " +
            "conditions. Give the answers in the format: " +
            "place 1;short description (up to 5 words)|place 2;short description|place 3;short description. " +
            "There is no need for any additional numbering and words, the answer is just one line. Conditions:%s";

    private static final String QUICK_REC_TEMPLATE_RUSSIAN = "Придумай мне ровно 3 варианта путешествий исходя из входных условий. Ответ выдай в формате: место1;описание|место2;описание|место3;описание. Описание не должно содержать более 5 слов. Пример: Париж; город любви и романтики|Рим; вечный город с богатой историей|Барселона; город, где слились история и современность. Не надо никаких дополнительных нумераций и слов, ответ просто одной строкой на русском языке. Условия: %s";

    private static final String DETAILED_REC_TEMPLATE_RUSSIAN = "У меня есть следующие %d варианта для путешествия: %s. Мои пожелания для путешествия следующие: purpose=Family trip;destination=Asia;weather=warm;budget=Under 1000$-3000$;companions=family;duration=7-14 days. Дай мне исходя из этих предпочтений подробное описание этих вариантов в формате JSON (не надо никаких дополнительных нумераций и слов, в ответе только JSON). Формат: {options: {placeName: НАЗВАНИЕ МЕСТА, budget: НЕОБХОДИМЫЙ БЮДЖЕТ, reasoning: ПОЧЕМУ ЭТОТ ВАРИАНТ ПОДХОДИТ, creativeDescription: КРЕАТИВНОЕ ХУДОЖЕСТВЕННОЕ ОПИСАНИЕ ВАРИАНТА, tips: ОБЩИЕ РЕКОМЕНДАЦИИ, whereToGo: [],ДОСТОПРИМЕЧАТЕЛЬНОСТИ/КОНКРЕТНЫЕ МЕСТА РЕКОМЕНДУЕМЫЕ ДЛЯ ПОСЕЩЕНИЯ], additionalConsideration: ЧТО НУЖНО ДОПОЛНИТЕЛЬНО УЧЕСТЬ}}";

    public TravelRecommendation createQuickRecommendation(Long inquiryId, String inquiryPayload) {
        var prompt = String.format(QUICK_REC_TEMPLATE_RUSSIAN, inquiryPayload);
        var answer = gptHttpClient.ask(prompt);
        var recommendation = TravelRecommendation.builder()
                .inquiryId(inquiryId)
                .quickPayload(answer)
                .createdAt(Instant.now())
                .build();

        return recommendationRepository.save(recommendation);
    }

    @SneakyThrows
    @Async
    public void createDetailedRecommendationAsync(Long recommendationId, String quickRecommendationPayload) {
        var quickOptions = toDtoQuickList(quickRecommendationPayload);
        StringBuilder promptOptionsBuilder = new StringBuilder();

        for (int i = 0; i < quickOptions.size(); i++) {
            promptOptionsBuilder.append(i + 1)
                    .append(") ")
                    .append(quickOptions.get(i).placeName())
                    .append(" - ")
                    .append(quickOptions.get(i).shortDescription())
                    .append(";");
        }

        var prompt = String.format(DETAILED_REC_TEMPLATE_RUSSIAN, quickOptions.size(), promptOptionsBuilder);
        var detailedPayload = gptHttpClient.ask(prompt);
        var recommendation = recommendationRepository.findById(recommendationId).orElseThrow();
        recommendation.setDetailedPayload(detailedPayload);
        log.info("Save {} with detailed payload {}", recommendationId, detailedPayload);
        recommendationRepository.save(recommendation);
    }

    TravelRecommendation findByInquiryId(Long inquiryId) {
        return recommendationRepository.findByInquiryId(inquiryId);
    }

    @SneakyThrows
    public static List<TravelRecommendationQuickOptionDto> toDtoQuickList(String recommendationPayload) {
        recommendationPayload = recommendationPayload.replaceAll("\"", "");
        return Stream.of(recommendationPayload.split("\\|"))
                .map(it -> it.split(";"))
                .filter(TravelRecommendationService::isValid)
                .map(it -> new TravelRecommendationQuickOptionDto(it[0], it[1]))
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

    private static boolean isValid(String[] it) {
        try {
            return !it[0].isEmpty() && !it[1].isEmpty();
        } catch (RuntimeException e) {
            log.error(e.getMessage());

            return false;
        }
    }
}