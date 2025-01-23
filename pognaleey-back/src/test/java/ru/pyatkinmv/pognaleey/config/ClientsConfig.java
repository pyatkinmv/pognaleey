package ru.pyatkinmv.pognaleey.config;

import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.pyatkinmv.pognaleey.client.GptHttpClient;
import ru.pyatkinmv.pognaleey.client.YandexImagesSearchHttpClient;
import ru.pyatkinmv.pognaleey.dto.SearchImageDto;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static ru.pyatkinmv.pognaleey.service.PromptService.*;

@Configuration
public class ClientsConfig {

    @Bean
    public GptHttpClient gptHttpClient() {
        var gptHttpClientMock = Mockito.mock(GptHttpClient.class);

        var shortRecommendationRaw = "{Грузия, Тбилиси и винные регионы}(Грузия весна пейзаж)|{Париж, Франция: Город любви}(Париж Эйфелева башня закат)|{Красная Поляна, Сочи: Горнолыжный отдых}(Красная Поляна лыжи снег)" +
                "|{Индонезия, Бали: Пляжный отдых}(Бали пляжи закат)|{Исландия: Природные чудеса}(Исландия водопады ледники)";
        var detailedRecommendationRaw = """
                {
                "reasoning":"Токио предлагает уникальное сочетание современных технологий...",
                "description":"Окунитесь в мир контрастов, где небоскрёбы возвышаются над древними храмами..."
                }
                """;
        var guideImagesRaw = "{Сабантуй в Казани}(Сабантуй Казань празднование)|{Летние пейзажи Казани}(Казань лето пейзаж)|{Пиво в Татарстане}(Пиво Татарстан кружка на фестивале)";
        var guideRaw = """
                # {Сабантуй в Казани}
                text1
                # Летние пейзажи Казани
                {Летние пейзажи Казани}
                text2
                # Пиво в Татарстане
                {Пиво в Татарстане}
                text3""";
        when(gptHttpClientMock.ask(ArgumentMatchers.contains(QUICK_PROMPT_FORMAT.substring(0, 10))))
                .thenReturn(shortRecommendationRaw);
        when(gptHttpClientMock.ask(ArgumentMatchers.contains(DETAILED_PROMPT_FORMAT.substring(0, 10))))
                .thenReturn(detailedRecommendationRaw);
        when(gptHttpClientMock.ask(ArgumentMatchers.contains(GUIDE_IMAGES_PROMPT_FORMAT.substring(0, 10))))
                .thenReturn(guideImagesRaw);
        when(gptHttpClientMock.ask(ArgumentMatchers.contains(GUIDE_PROMPT_FORMAT.substring(0, 10))))
                .thenReturn(guideRaw);

        return gptHttpClientMock;
    }

    @Bean
    public YandexImagesSearchHttpClient imagesSearchHttpClient() {
        var imageSearchHttpClientMock = Mockito.mock(YandexImagesSearchHttpClient.class);
        when(imageSearchHttpClientMock.searchImage(any()))
                .thenReturn(Optional.of(new SearchImageDto("imageUrl", "imageUrl")));

        return imageSearchHttpClientMock;
    }
}

