package ru.pyatkinmv.pognaleey.config;

import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.pyatkinmv.pognaleey.client.GptHttpClient;
import ru.pyatkinmv.pognaleey.client.ImagesSearchHttpClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static ru.pyatkinmv.pognaleey.service.PromptService.DETAILED_PROMPT_FORMAT;
import static ru.pyatkinmv.pognaleey.service.PromptService.QUICK_PROMPT_FORMAT;

@TestConfiguration
public class ClientsConfiguration {

    @Bean
    public GptHttpClient gptHttpClient() {
        var gptHttpClientMock = Mockito.mock(GptHttpClient.class);

        var shortRecommendationRaw =
                "Токио;современный мегаполис|Сингапур;город-государство контрастов|Бангкок;тайский колорит и экзотика";
        var detailedRecommendationRaw = """
                {"title":"Токио","budget":"3800$","reasoning":"Токио предлагает уникальное сочетание современных технологий и древних традиций, что делает его идеальным местом для тех, кто хочет увидеть как исторические достопримечательности, так и современные развлечения.","creativeDescription":"Окунитесь в мир контрастов, где небоскрёбы возвышаются над древними храмами, а современные технологии соседствуют с традиционными обычаями. Прогуляйтесь по оживлённым улицам Синдзюку, посетите храм Сэнсо-дзи и насладитесь красотой природы в парке Хамарикю. Не упустите возможность попробовать блюда японской кухни и посетить знаменитые торговые центры.","tips":"Планируйте свой маршрут заранее, чтобы успеть посетить все интересующие вас места. Используйте общественный транспорт для передвижения по городу, это позволит вам сэкономить время и деньги. Обратите внимание на сезонные фестивали и мероприятия, которые проходят весной в Токио.","whereToGo":["Синдзюку","Храм Сэнсо-дзи","Парк Хамарикю","Токийская башня","Императорский дворец","Акихабара"],"additionalConsideration":"Учтите, что некоторые достопримечательности могут быть закрыты в определённые дни или часы. Также рекомендуется иметь при себе карту города или использовать навигационные приложения."}
                """;
        when(gptHttpClientMock.ask(ArgumentMatchers.contains(QUICK_PROMPT_FORMAT.substring(0, 8))))
                .thenReturn(shortRecommendationRaw);
        when(gptHttpClientMock.ask(ArgumentMatchers.contains(DETAILED_PROMPT_FORMAT.substring(0, 8))))
                .thenReturn(detailedRecommendationRaw);

        return gptHttpClientMock;
    }

    @Bean
    public ImagesSearchHttpClient imagesSearchHttpClient() {
        var imageSearchHttpClientMock = Mockito.mock(ImagesSearchHttpClient.class);
        when(imageSearchHttpClientMock.searchImageUrl(any())).thenReturn("imageUrl");

        return imageSearchHttpClientMock;
    }
}

