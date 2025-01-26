package ru.pyatkinmv.pognaleey.service;

import ru.pyatkinmv.pognaleey.dto.GptResponseRecommendationDetailsDto;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;
import ru.pyatkinmv.pognaleey.util.Utils;

public class PromptService {

    public static final String QUICK_PROMPT_FORMAT = """
            Придумай мне ровно %d вариантов путешествий исходя из входных условий: %s.К этим вариантам добавь короткие
             поисковые запросы (search query) на АНГЛИЙСКОМ языке, по которым я могу найти красивые картинки в соответствии с основными условиями.
             Ответ выдай в формате:{заглавие1}(search query1)|{заглавие2}(search query2)|...
             Например: {Грузия, Тбилиси и винные регионы}(Georgia spring landscape)
            |{Париж, Франция: Город любви}(Paris Eiffel Tower sunset)
            |{Красная Поляна, Сочи: Горнолыжный отдых}(Krasnaya Polyana skiing snow).
            В ответе не пиши ничего кроме вариантов
            """;
    public static final String DETAILED_PROMPT_FORMAT = """
            У меня есть идея для путешествия:%s.Мои пожелания следующие: %s.
            Дай мне исходя из этих пожеланий подробное описание в формате JSON
            (не надо никаких дополнительных нумераций и слов, в ответе только JSON указанного формата). Формат: %s""";
    static final GptResponseRecommendationDetailsDto DETAILED_PROMPT_OBJ = new GptResponseRecommendationDetailsDto(
            "ПОЧЕМУ ЭТОТ ВАРИАНТ ПОДХОДИТ",
            "ОПИСАНИЕ ВАРИАНТА"
    );


    public static final String GUIDE_IMAGES_PROMPT_FORMAT = """
            Пишу путеводитель-статью по теме: %s. Условия: %s.
            Хочу добавить красивые картинки в статью. Предложи мне список 5 поисковых запросов на АНГЛИЙСКОМ языке,
             по которым я смогу найти красивые картинки в соответствии с темой и условиями.
             К этим запросам добавь более короткие описания на русском языка, которые я буду использовать в статье как заглавия.
             В качестве разделителя используй пайп —|.
             Ответ выдай в формате:{описание1}(search query1)|{описание2}(search query2)|...
             То есть описание в фигурных скобках, поисковой запрос в круглых.
             Например, {Запретный город}(Beijing Forbidden City)|{Хутоны}(Beijing Hutongs in spring)|{Чайная церемония}(Kyoto geisha tea ceremony).
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


    public static final String GUIDE_INTRO_PROMPT_FORMAT = """
            Пишу статью-путеводитель по теме:%s.Условия путешествия:%s.
            Напиши введение для этой статьи в формате Markdown.
            Цель:заинтересовать читателя, представить место, дать краткое описание и вызвать желание узнать больше.
            Содержание:Краткое описание места,почему это место стоит посетить (основные особенности, уникальность).
            В ответе не пиши ничего кроме текста введения.
            Пример: \\"## Введение: волшебство Токио в весенний период...\\n текст введения \\"
            """;
    // Пишу путеводитель-статью по теме: Токио. Условия: duration=8-14 дней;preferences=[мегаполис, прогулки];purpose=[отдых];companions=с партнёром;locationTo=asia;season=весна;transport=[any];budget=standard;locationFrom=Москва.Сгенерируй 5 заголовков, готовые к использованию в тексте статьи. Эти заголовки должны относится к практической части статьи (информация по подготовке, как добраться,детализированный бюджет,где расположиться, приложения/сайты для путешественников; и если релевантно:виза,валюта,риски,безопасность,полезные советы,необходимые вещи,что нужно знать и тд. Не включай сюда информацию по достопримечательностям,местам для посещения,маршрутам,введению и заключению. В ответе не пиши ничего кроме ответа указанного формата:Заголовок1|Заголовок2|Заголовок3|...
    public static final String GUIDE_PRACTICAL_TITLES_PROMPT_FORMAT = """
            Пишу путеводитель-статью по теме:%s.Условия путешествия:%s.
            Сгенерируй %d заголовков, готовые к использованию в тексте статьи.
            Эти заголовки должны относится к практической части статьи:информация по подготовке,как добраться,
            детализированный бюджет,где расположиться,приложения/сайты для путешественников;и если релевантно:
            виза,валюта,риски,безопасность,полезные советы,необходимые вещи,что нужно знать и тд.
            Не включай сюда информацию по достопримечательностям,местам для посещения,маршрутам,введению и заключению.
            В ответе не пиши ничего кроме ответа указанного формата:Заголовок1|Заголовок2|Заголовок3|...
            """;
    public static final int GUIDE_PRACTICAL_TITLES_COUNT = 5;
    public static final String GUIDE_PRACTICAL_TITLE_PROMPT_FORMAT = """
            Пишу путеводитель-статью по теме:%s.Условия путешествия:%s.
            У меня есть следующие темы:%s.Сгенерируй мне текст по одной из тем:%s.
            Но не включай информацию по другим темам из списка.
            Если в теме описывается бюджет и цены,немного преувеличивай.Формат Markdown.
            В ответе не пиши ничего кроме текста ответа указанного формата:### %s\\n текст
            """;
    public static final String GUIDE_VISUAL_PROMPT_FORMAT = """
            Пишу статью-путеводитель по теме:%s.Условия путешествия:%s.
            Напиши часть статьи, которая относится к рекомендованным маршрутам,достопримечательностям,развлечениям,активностям.
            В формате Markdown.Также включи следующие %d тем:%s.
            Добавь для этих тем заголовки, между заголовком и текстом добавь её саму в фигурных скобках.
            Формат: \\"## общий заголовок\\n### тема из списка {тема из списка буква в букву}\\n описание\\".
            В ответе не пиши ничего кроме текста статьи.Заголовок самой статьи добавлять не нужно
            """;
    public static final String GUIDE_CONCLUSION_PROMPT_FORMAT = """
            Пишу путеводитель-статью по теме:%s.Условия путешествия:%s.
            Напиши введение для этой статьи в формате Markdown.
            Цель:подвести итог, дать финальные рекомендации и вдохновить читателя на путешествие.
            Содержание:Краткое резюме о месте и его ключевых особенностях,подвести итог,оставить читателя с вдохновляющим настроем.
            В ответе не пиши ничего кроме текста заключения. Пример: \\"## Заключение\\n текст заключения \\"
            """;

    public static String generateGuideIntroPrompt(String guideTitle, String inquiryParams) {
        return String.format(GUIDE_INTRO_PROMPT_FORMAT, guideTitle, inquiryParams);
    }

    public static String generateGuidePracticalTitlesPrompt(String guideTitle, String inquiryParams) {
        return String.format(GUIDE_PRACTICAL_TITLES_PROMPT_FORMAT, guideTitle, inquiryParams, GUIDE_PRACTICAL_TITLES_COUNT);
    }

    public static String generateGuidePracticalTitlePrompt(String guideTitle, String inquiryParams, String allTitles, String practicalTitle) {
        return String.format(GUIDE_PRACTICAL_TITLE_PROMPT_FORMAT, guideTitle, inquiryParams, allTitles, practicalTitle, practicalTitle);
    }

    public static String generateGuideVisualPrompt(String guideTitle, String inquiryParams, String guideVisualTopics) {
        return String.format(GUIDE_VISUAL_PROMPT_FORMAT, guideTitle, inquiryParams, GUIDE_PRACTICAL_TITLES_COUNT, guideVisualTopics);
    }

    public static String generateGuideConclusionPrompt(String guideTitle, String inquiryParams) {
        return String.format(GUIDE_CONCLUSION_PROMPT_FORMAT, guideTitle, inquiryParams);
    }

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
        return recommendation.getTitle();
    }

    public static String generateGuideImagesPrompt(String guideTitle, String inquiryParams) {
        return String.format(GUIDE_IMAGES_PROMPT_FORMAT, guideTitle, inquiryParams);
    }

    public static String generateCreateGuidePrompt(String title, String inquiryParams, String guideTopics) {
        return String.format(GUIDE_PROMPT_FORMAT, title, inquiryParams, guideTopics);
    }
}
