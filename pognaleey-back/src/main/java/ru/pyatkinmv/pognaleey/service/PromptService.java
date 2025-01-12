package ru.pyatkinmv.pognaleey.service;

import ru.pyatkinmv.pognaleey.dto.GptResponseRecommendationDetailsDto;
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

    public static final String QUICK_PROMPT_FORMAT = """
            Придумай мне ровно %d варианта путешествий исходя из входных условий: %s.К этим вариантам добавь короткие
             поисковые запросы, по которым я могу найти красивые картинки в соответствии с основными условиями.
             Ответ выдай в формате:{заглавие1}(поисковой запрос1)|{заглавие2}(поисковой запрос2)|...
             Например: {Грузия, Тбилиси и винные регионы}(Грузия весна пейзаж)
            |{Париж, Франция: Город любви}(Париж Эйфелева башня закат)
            |{Красная Поляна, Сочи: Горнолыжный отдых}(Красная Поляна лыжи снег).
            В ответе не пиши ничего кроме самой статьи
            """;
    public static final String DETAILED_PROMPT_FORMAT = """
            У меня есть идея для путешествия:%s.Мои пожелания следующие: %s.
            Дай мне исходя из этих предпочтений подробное описание в формате JSON
            (не надо никаких дополнительных нумераций и слов, в ответе только JSON). Формат: %s""";
    public static final String GUIDE_IMAGES_PROMPT_FORMAT = """
            Пишу путеводитель-статью по теме: %s. Условия: %s.
            Хочу добавить красивые картинки в статью. Предложи мне список 5 поисковых запросов,
             по которым я смогу найти красивые картинки в соответствии с темой и условиями.
             К этим запросам добавь более короткие описания, которые я буду использовать в статье как заглавия.
             В качестве разделителя используй пайп —|.
             Ответ выдай в формате:{описание1}(поисковой запрос 1)|{описание2}(поисковой запрос 2)|...
             Например, {Запретный город}(Запретный город на рассвете Пекин)|{Хутоны}(Пекинские хутуны весной)|{Чайная церемония}(Киото чайная церемония гейши).
             В ответе не пиши ничего кроме самой статьи
            """;
    public static final String GUIDE_PROMPT_FORMAT = """
            Напиши увлекательную, полезную статью-путеводитель по теме: %s.
            Статья должна в себя включать следующее: введение,практическая информация по подготовке,достопримечательности и маршруты,заключение.
            Также можно добавить:бюджет с детализацией,советы для комфортного путешествия,погода,транспорт,
            риски и безопасность,документы и виза,необходимые вещи,интересные факты,дополнительные материалы(конкретные приложения или ресурсы).
            Не следуй буквально этой структуре, будь креативным в организации статьи, выборе параграфов и их порядке.
            Условия для путешествия:%s.
            Используй в статье следующие 5 тем:%s.
            Добавь для этих 5 тем заголовки, между заголовком и текстом добавь её саму в фигурных скобках,
            например: "заголовок \n{тема из списка буква в букву} \nописание".
            Также выдели фигурными скобками {} само заглавие статьи в самом начале.
            Не используй повелительное наклонение. В ответе не пиши ничего кроме самой статьи.Формат статьи — Markdown
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

    public static String generateGuideImagesPrompt(String guideTitle, String inquiryParams) {
        return String.format(GUIDE_IMAGES_PROMPT_FORMAT, guideTitle, inquiryParams);
    }

    public static String generateCreateGuidePrompt(String title, String inquiryParams, String guideTopics) {
        return String.format(GUIDE_PROMPT_FORMAT, title, inquiryParams, guideTopics);
    }
}
