package ru.pyatkinmv.pognaleey.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.pyatkinmv.pognaleey.client.GptHttpClient;
import ru.pyatkinmv.pognaleey.client.ImagesSearchHttpClient;
import ru.pyatkinmv.pognaleey.model.TravelInquiry;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;
import ru.pyatkinmv.pognaleey.repository.TravelInquiryRepository;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static ru.pyatkinmv.pognaleey.service.PromptService.DETAILED_PROMPT_FORMAT;
import static ru.pyatkinmv.pognaleey.service.PromptService.SHORT_PROMPT_FORMAT;

@SpringBootTest
class TravelRecommendationServiceTest {
    @Autowired
    private TravelRecommendationService travelRecommendationService;
    @Autowired
    private TravelInquiryRepository travelInquiryRepository;
    @MockitoBean
    private GptHttpClient gptHttpClient;
    @MockitoBean
    private ImagesSearchHttpClient imagesSearchHttpClient;

    @BeforeEach
    void setUp() {
        var shortRecommendationRaw = "Токио;современный мегаполис с древними традициями|Сингапур;город-государство контрастов|Бангкок;тайский колорит и экзотика";
        var detailedRecommendationRaw = """
                {"recommendations":[{"title":"Токио","budget":{"from":2100,"to":3800},"reasoning":"Токио предлагает уникальное сочетание современных технологий и древних традиций, что делает его идеальным местом для тех, кто хочет увидеть как исторические достопримечательности, так и современные развлечения.","creativeDescription":"Окунитесь в мир контрастов, где небоскрёбы возвышаются над древними храмами, а современные технологии соседствуют с традиционными обычаями. Прогуляйтесь по оживлённым улицам Синдзюку, посетите храм Сэнсо-дзи и насладитесь красотой природы в парке Хамарикю. Не упустите возможность попробовать блюда японской кухни и посетить знаменитые торговые центры.","tips":"Планируйте свой маршрут заранее, чтобы успеть посетить все интересующие вас места. Используйте общественный транспорт для передвижения по городу, это позволит вам сэкономить время и деньги. Обратите внимание на сезонные фестивали и мероприятия, которые проходят весной в Токио.","whereToGo":["Синдзюку","Храм Сэнсо-дзи","Парк Хамарикю","Токийская башня","Императорский дворец","Акихабара"],"additionalConsideration":"Учтите, что некоторые достопримечательности могут быть закрыты в определённые дни или часы. Также рекомендуется иметь при себе карту города или использовать навигационные приложения.","imageUrl":null},{"title":"Сингапур","budget":{"from":2100,"to":3800},"reasoning":"Сингапур — это город-государство, которое сочетает в себе культуру разных народов и предлагает множество развлечений и интересных мест для посещения.","creativeDescription":"Погрузитесь в атмосферу контрастов, где Восток встречается с Западом, а современность переплетается с традициями. Посетите Marina Bay Sands, прогуляйтесь по Orchard Road и насладитесь видами с Singapore Flyer. Попробуйте блюда местной кухни и посетите уникальные музеи и галереи.","tips":"Используйте карты и навигационные приложения для планирования маршрута и поиска интересных мест. Обратите внимание на культурные мероприятия и фестивали, которые могут проходить во время вашего пребывания.","whereToGo":["Marina Bay Sands","Orchard Road","Singapore Flyer","Universal Studios Singapore","Gardens by the Bay","Merlion Park"],"additionalConsideration":"Учитывайте, что в Сингапуре действует система штрафов за нарушение правил, поэтому важно соблюдать местные законы и нормы поведения.","imageUrl":null},{"title":"Бангкок","budget":{"from":2100,"to":3800},"reasoning":"Бангкок — это столица Таиланда, которая предлагает уникальный колорит и экзотику, а также множество интересных мест для посещения.","creativeDescription":"Почувствуйте атмосферу тайского колорита, прогуливаясь по узким улочкам Чайна-тауна, наслаждаясь видами с храмов и посещая рынки и базары. Попробуйте традиционные блюда тайской кухни и купите сувениры на память. Не забудьте посетить Большой дворец и Ват Пхо, чтобы ощутить дух буддизма.","tips":"Будьте осторожны с личными вещами и деньгами, особенно в людных местах. Используйте такси или общественный транспорт для перемещения по городу.","whereToGo":["Чайна-таун","Большой дворец","Ват Пхо","MBK Center","Chatuchak Weekend Market","Talad Rod Fai (Train Night Market)"],"additionalConsideration":"Обратите внимание на часы работы достопримечательностей и учтите, что некоторые из них могут быть закрыты на ремонт или реконструкцию.","imageUrl":null}]}
                """;
        when(gptHttpClient.ask(ArgumentMatchers.contains(SHORT_PROMPT_FORMAT.substring(0, 8))))
                .thenReturn(shortRecommendationRaw);
        when(gptHttpClient.ask(ArgumentMatchers.contains(DETAILED_PROMPT_FORMAT.substring(0, 8))))
                .thenReturn(detailedRecommendationRaw);
        when(imagesSearchHttpClient.searchImageUrl(any())).thenReturn("imageUrl");
    }

    @Test
    void createShortRecommendations() {
        var inquiry = createTravelInquiry();
        var recommendations =
                travelRecommendationService.createShortRecommendations(inquiry.getId(), inquiry.getParams());
        assertThat(recommendations)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id", "createdAt")
                .containsExactly(
                        new TravelRecommendation(null, null, inquiry.getId(), "Токио", "современный мегаполис с древними традициями", null, null),
                        new TravelRecommendation(null, null, inquiry.getId(), "Сингапур", "город-государство контрастов", null, null),
                        new TravelRecommendation(null, null, inquiry.getId(), "Бангкок", "тайский колорит и экзотика", null, null)
                );
    }

    @Test
    void enrichWithDetailsAsync() {
        var inquiry = createTravelInquiry();
        var recommendations =
                travelRecommendationService.createShortRecommendations(inquiry.getId(), inquiry.getParams());
        assertThat(recommendations).allMatch(it -> it.getDetails() == null);
        travelRecommendationService.enrichWithDetailsAsync(recommendations, inquiry.getParams());

        var recommendationIds = recommendations.stream().map(TravelRecommendation::getId).toList();
        var updated = travelRecommendationService.findAllByIds(recommendationIds);
        assertThat(updated).hasSize(3);
        assertThat(updated).allMatch(it -> it.getDetails() != null);
    }

    @Test
    void enrichWithImagesAsync() {
        var inquiry = createTravelInquiry();
        var recommendations =
                travelRecommendationService.createShortRecommendations(inquiry.getId(), inquiry.getParams());
        assertThat(recommendations).allMatch(it -> it.getImageUrl() == null);
        travelRecommendationService.enrichWithImagesAsync(recommendations);

        var recommendationIds = recommendations.stream().map(TravelRecommendation::getId).toList();
        var updated = travelRecommendationService.findAllByIds(recommendationIds);
        assertThat(updated).hasSize(3);
        assertThat(updated).allMatch(it -> it.getImageUrl() != null);
    }

    private TravelInquiry createTravelInquiry() {
        var inquiryParams = "duration=8-14 days;from=Moscow;to=Asia;budget={from=2100, to=3800}";
        var inquiry = travelInquiryRepository.save(new TravelInquiry(null, inquiryParams, Instant.now()));
        assertThat(inquiry.getParams()).isEqualTo(inquiryParams);
        assertThat(inquiry.getId()).isNotNull();

        return inquiry;
    }
}