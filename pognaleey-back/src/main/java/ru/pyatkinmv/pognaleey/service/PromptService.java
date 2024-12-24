package ru.pyatkinmv.pognaleey.service;

import org.springframework.stereotype.Service;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class PromptService {

    private static final String SHORT_PROMPT_FORMAT = """
            Придумай мне ровно %d варианта путешествий исходя из входных условий. 
            Ответ выдай в формате: место1;описание|место2;описание|место3;описание. 
            Описание не должно содержать более 5 слов. 
            Пример: Париж;город любви и романтики|Рим;вечный город с богатой историей|Барселона;история и современность. 
            Не надо никаких дополнительных нумераций и слов, ответ просто одной строкой на русском языке. Условия: %s"
            """;

    private static final String DETAILED_PROMPT_FORMAT = """
            У меня есть следующие %d варианта для путешествия: %s. Мои пожелания для путешествия следующие: %s.
            Дай мне исходя из этих предпочтений подробное описание этих вариантов в формате JSON
            (не надо никаких дополнительных нумераций и слов, в ответе только JSON).
            Формат:
            {recommendations:
                [
                    {
                        title: НАЗВАНИЕ МЕСТА,
                        budget: {from: БЮДЖЕТ ОТ, to: БЮДЖЕТ ДО},
                        reasoning: ПОЧЕМУ ЭТОТ ВАРИАНТ ПОДХОДИТ,
                        creativeDescription: КРЕАТИВНОЕ ХУДОЖЕСТВЕННОЕ ОПИСАНИЕ ВАРИАНТА,
                        tips: ОБЩИЕ РЕКОМЕНДАЦИИ (СТРОКА),
                        whereToGo: [ДОСТОПРИМЕЧАТЕЛЬНОСТИ/КОНКРЕТНЫЕ МЕСТА РЕКОМЕНДУЕМЫЕ ДЛЯ ПОСЕЩЕНИЯ],
                        additionalConsideration: ЧТО НУЖНО ДОПОЛНИТЕЛЬНО УЧЕСТЬ
                    }
                ]
            }""";

    public String getShortPrompt(int optionsNumber, String inquiryParams) {
        return String.format(SHORT_PROMPT_FORMAT, optionsNumber, inquiryParams)
                .replaceAll("\n", "").replaceAll("\t", "");
    }

    public String getDetailedPrompt(List<TravelRecommendation> recommendations, String inquiryParams) {
        var recommendationsStr = toPromtString(recommendations);

        return String.format(DETAILED_PROMPT_FORMAT, recommendations.size(), recommendationsStr, inquiryParams)
                .replaceAll("\n", "").replaceAll("\t", "");
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
