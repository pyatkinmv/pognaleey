package ru.pyatkinmv.pognaleey.service;

import ru.pyatkinmv.pognaleey.dto.gpt.GptResponseRecommendationDetailsDto;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;
import ru.pyatkinmv.pognaleey.util.Utils;

import java.util.List;

public class PromptService {

    static final GptResponseRecommendationDetailsDto DETAILED_PROMPT_OBJ = new GptResponseRecommendationDetailsDto(
            "НАЗВАНИЕ МЕСТА",
            "НЕОБХОДИМЫЙ БЮДЖЕТ (НАПРИМЕР, 1000$)",
            "ПОЧЕМУ ЭТОТ ВАРИАНТ ПОДХОДИТ",
            "КРЕАТИВНОЕ ХУДОЖЕСТВЕННОЕ ОПИСАНИЕ ВАРИАНТА",
            "ОБЩИЕ РЕКОМЕНДАЦИИ (СТРОКА)",
            List.of("ДОСТОПРИМЕЧАТЕЛЬНОСТИ/КОНКРЕТНЫЕ МЕСТА РЕКОМЕНДУЕМЫЕ ДЛЯ ПОСЕЩЕНИЯ"),
            "ЧТО НУЖНО ДОПОЛНИТЕЛЬНО УЧЕСТЬ"
    );

    static final String DETAILED_PROMPT_FORMAT = """
            У меня есть идея для путешествия:%s.Мои пожелания следующие: %s.
            Дай мне исходя из этих предпочтений подробное описание в формате JSON
            (не надо никаких дополнительных нумераций и слов, в ответе только JSON). Формат: %s""";

    static final String QUICK_PROMPT_FORMAT = """
            Придумай мне ровно %d варианта путешествий исходя из входных условий: %s.
            Ответ выдай в формате: место1;описание|место2;описание|место3;описание.
            Описание не должно содержать более 4 слов.
            Пример: Париж;город любви и романтики|Рим;город с богатой историей|Барселона;история и современность
            """;

    public static String generateQuickPrompt(int optionsNumber, String inquiryParams) {
        return String.format(QUICK_PROMPT_FORMAT, optionsNumber, inquiryParams)
                .replaceAll("\n", "");
    }

    public static String generateDetailedPrompt(TravelRecommendation recommendation, String inquiryParams) {
        var recommendationsStr = toPromtString(recommendation);

        return String.format(DETAILED_PROMPT_FORMAT, recommendationsStr, inquiryParams,
                        Utils.toJson(DETAILED_PROMPT_OBJ))
                .replaceAll("\n", "").replaceAll("   ", "");
    }

    private static String toPromtString(TravelRecommendation recommendation) {
        return recommendation.getTitle() + "—" + recommendation.getShortDescription().trim();
    }
}
