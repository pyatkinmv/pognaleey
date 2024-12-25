package ru.pyatkinmv.pognaleey.service;

import ru.pyatkinmv.pognaleey.dto.gpt.GptResponseQuickRecommendationListDto;
import ru.pyatkinmv.pognaleey.dto.gpt.GptResponseQuickRecommendationListDto.GptResponseQuickRecommendationDto;
import ru.pyatkinmv.pognaleey.dto.gpt.GptResponseRecommendationDetailsListDto;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;
import ru.pyatkinmv.pognaleey.util.Utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PromptService {

    static final GptResponseRecommendationDetailsListDto DETAILED_PROMPT_OBJ = new GptResponseRecommendationDetailsListDto(
            List.of(
                    new GptResponseRecommendationDetailsListDto.GptResponseRecommendationDetailsDto(
                            "НАЗВАНИЕ МЕСТА",
                            new GptResponseRecommendationDetailsListDto.Budget("БЮДЖЕТ ОТ", "БЮДЖЕТ ДО"),
                            "ПОЧЕМУ ЭТОТ ВАРИАНТ ПОДХОДИТ",
                            "КРЕАТИВНОЕ ХУДОЖЕСТВЕННОЕ ОПИСАНИЕ ВАРИАНТА",
                            "ОБЩИЕ РЕКОМЕНДАЦИИ (СТРОКА)",
                            List.of("ДОСТОПРИМЕЧАТЕЛЬНОСТИ/КОНКРЕТНЫЕ МЕСТА РЕКОМЕНДУЕМЫЕ ДЛЯ ПОСЕЩЕНИЯ"),
                            "ЧТО НУЖНО ДОПОЛНИТЕЛЬНО УЧЕСТЬ"
                    )
            )
    );

    static final GptResponseQuickRecommendationListDto QUICK_PROMPT_OBJ = new GptResponseQuickRecommendationListDto(
            List.of(
                    new GptResponseQuickRecommendationDto("МЕСТО", "ОПИСАНИЕ")
            )
    );

    static final String DETAILED_PROMPT_FORMAT = """
            У меня есть следующие %d варианта для путешествия: %s.Мои пожелания для путешествия следующие: %s.
            Дай мне исходя из этих предпочтений подробное описание этих вариантов в формате JSON
            (не надо никаких дополнительных нумераций и слов, в ответе только JSON). Формат: %s""";

    static final String SHORT_PROMPT_FORMAT = """
            Придумай мне ровно %d варианта путешествий исходя из входных условий.
            Ответ выдай в формате: %s.Описание не должно содержать более 5 слов.Условия: %s
            """;

    public static String generateShortPrompt(int optionsNumber, String inquiryParams) {
        return String.format(SHORT_PROMPT_FORMAT, optionsNumber, Utils.toJson(QUICK_PROMPT_OBJ), inquiryParams)
                .replaceAll("\n", "");
    }

    public static String generateDetailedPrompt(List<TravelRecommendation> recommendations, String inquiryParams) {
        var recommendationsStr = toPromtString(recommendations);

        return String.format(DETAILED_PROMPT_FORMAT, recommendations.size(), recommendationsStr, inquiryParams,
                        Utils.toJson(DETAILED_PROMPT_OBJ))
                .replaceAll("\n", "").replaceAll("   ", "");
    }

    private static String toPromtString(List<TravelRecommendation> recommendations) {
        return IntStream.range(0, recommendations.size())
                .mapToObj(i -> String.format("%d)%s—%s",
                        i + 1,
                        recommendations.get(i).getTitle(),
                        recommendations.get(i).getShortDescription()))
                .collect(Collectors.joining(";"));
    }
}
