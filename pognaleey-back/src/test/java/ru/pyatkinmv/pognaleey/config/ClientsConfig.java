package ru.pyatkinmv.pognaleey.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.pyatkinmv.pognaleey.client.GptHttpClient;
import ru.pyatkinmv.pognaleey.client.PixabayImageSearchHttpClient;
import ru.pyatkinmv.pognaleey.dto.ImageSearchClientImageDto;

@Configuration
public class ClientsConfig {

  @Bean
  public GptHttpClient gptHttpClient() {
    var gptHttpClientMock = Mockito.mock(GptHttpClient.class);

    var shortRecommendationRaw =
        "{Грузия, Тбилиси и винные регионы}(Грузия весна пейзаж)|{Париж, Франция: Город"
            + " любви}(Париж Эйфелева башня закат)|{Красная Поляна, Сочи: Горнолыжный"
            + " отдых}(Красная Поляна лыжи снег)|{Индонезия, Бали: Пляжный отдых}(Бали пляжи"
            + " закат)|{Исландия: Природные чудеса}(Исландия водопады ледники)";
    var detailedRecommendationRaw =
        """
{
"reasoning":"Токио предлагает уникальное сочетание современных технологий...",
"description":"Окунитесь в мир контрастов, где небоскрёбы возвышаются над древними храмами..."
}
""";
    var guideImagesRaw =
        "{Сабантуй в Казани}(Сабантуй Казань празднование)|{Летние пейзажи Казани}(Казань лето"
            + " пейзаж)|{Пиво в Татарстане}(Пиво Татарстан кружка на фестивале)";
    var guideRaw =
        """
        # {Сабантуй в Казани}
        text1
        # Летние пейзажи Казани
        {Летние пейзажи Казани}
        text2
        # Пиво в Татарстане
        {Пиво в Татарстане}
        text3\
        """;
    var sightseeingRaw =
        """
        ## Достопримечательности и маршруты Казани
        ### Проспект Баумана
        text1
        {Проспект Баумана}
        ### Казанский кремль
        text2
        {Казанский кремль}
        ### Башня Сююмбике
        text3
        {Башня Сююмбике}
        """;
    var practicalTopic = "### Практическая тема\n...";
    var practicalTopics = "Бюджет|Риски|Виза|Как добраться|Отели";
    String intro = "## Введение\n...";
    String conclusion = "## Заключение\n...";

    when(gptHttpClientMock.ask(ArgumentMatchers.matches(".*Придумай мне.*вариантов путешествий.*")))
        .thenReturn(shortRecommendationRaw);
    when(gptHttpClientMock.ask(
            ArgumentMatchers.matches(".*Дай мне.*подробное описание в формате JSON.*")))
        .thenReturn(detailedRecommendationRaw);
    when(gptHttpClientMock.ask(
            ArgumentMatchers.matches(".*Предложи мне список.*запросов.*красивые картинки.*")))
        .thenReturn(guideImagesRaw);
    when(gptHttpClientMock.ask(
            ArgumentMatchers.matches(".*Напиши увлекательную, полезную статью.*")))
        .thenReturn(guideRaw);
    when(gptHttpClientMock.ask(
            ArgumentMatchers.matches(
                ".*Напиши часть статьи.*относится к.*достопримечательностям.*")))
        .thenReturn(sightseeingRaw);
    when(gptHttpClientMock.ask(
            ArgumentMatchers.matches(".*заголовки.*к практической части статьи.*")))
        .thenReturn(practicalTopics);
    when(gptHttpClientMock.ask(
            ArgumentMatchers.matches(".*Сгенерируй мне текст по одной из тем.*")))
        .thenReturn(practicalTopic);
    when(gptHttpClientMock.ask(ArgumentMatchers.matches(".*Напиши введение.*"))).thenReturn(intro);
    when(gptHttpClientMock.ask(ArgumentMatchers.matches(".*Напиши заключение.*")))
        .thenReturn(conclusion);

    return gptHttpClientMock;
  }

  @Bean
  public PixabayImageSearchHttpClient imagesSearchHttpClient() {
    var imageSearchHttpClientMock = Mockito.mock(PixabayImageSearchHttpClient.class);
    when(imageSearchHttpClientMock.searchImage(any()))
        .thenAnswer(
            (Answer<Optional<ImageSearchClientImageDto>>)
                invocation -> {
                  var query = invocation.getArgument(0, String.class);
                  return Optional.of(
                      new ImageSearchClientImageDto("imageUrl", "thumbnailUrl", query));
                });
    return imageSearchHttpClientMock;
  }
}
