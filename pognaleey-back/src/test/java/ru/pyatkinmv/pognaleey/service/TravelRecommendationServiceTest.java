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
import ru.pyatkinmv.pognaleey.util.Utils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static ru.pyatkinmv.pognaleey.service.PromptService.*;

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
        var shortRecommendationRaw =
                "Токио;современный мегаполис|Сингапур;город-государство контрастов|Бангкок;тайский колорит и экзотика";
        var detailedRecommendationRaw = """
                {"title":"Токио","budget":"3800$","reasoning":"Токио предлагает уникальное сочетание современных технологий и древних традиций, что делает его идеальным местом для тех, кто хочет увидеть как исторические достопримечательности, так и современные развлечения.","creativeDescription":"Окунитесь в мир контрастов, где небоскрёбы возвышаются над древними храмами, а современные технологии соседствуют с традиционными обычаями. Прогуляйтесь по оживлённым улицам Синдзюку, посетите храм Сэнсо-дзи и насладитесь красотой природы в парке Хамарикю. Не упустите возможность попробовать блюда японской кухни и посетить знаменитые торговые центры.","tips":"Планируйте свой маршрут заранее, чтобы успеть посетить все интересующие вас места. Используйте общественный транспорт для передвижения по городу, это позволит вам сэкономить время и деньги. Обратите внимание на сезонные фестивали и мероприятия, которые проходят весной в Токио.","whereToGo":["Синдзюку","Храм Сэнсо-дзи","Парк Хамарикю","Токийская башня","Императорский дворец","Акихабара"],"additionalConsideration":"Учтите, что некоторые достопримечательности могут быть закрыты в определённые дни или часы. Также рекомендуется иметь при себе карту города или использовать навигационные приложения."}
                """;
        when(gptHttpClient.ask(ArgumentMatchers.contains(QUICK_PROMPT_FORMAT.substring(0, 8))))
                .thenReturn(shortRecommendationRaw);
        when(gptHttpClient.ask(ArgumentMatchers.contains(DETAILED_PROMPT_FORMAT.substring(0, 8))))
                .thenReturn(detailedRecommendationRaw);
        when(imagesSearchHttpClient.searchImageUrl(any())).thenReturn("imageUrl");
    }

    @Test
    void createQuickRecommendations() {
        var inquiry = createTravelInquiry();
        var recommendations =
                travelRecommendationService.createQuickRecommendations(inquiry.getId(), inquiry.getParams());
        assertThat(recommendations)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id", "createdAt")
                .containsExactly(
                        new TravelRecommendation(null, null, inquiry.getId(), "Токио", "современный мегаполис", null, null),
                        new TravelRecommendation(null, null, inquiry.getId(), "Сингапур", "город-государство контрастов", null, null),
                        new TravelRecommendation(null, null, inquiry.getId(), "Бангкок", "тайский колорит и экзотика", null, null)
                );
    }

    @Test
    void enrichWithDetailsAsync() {
        var inquiry = createTravelInquiry();
        var recommendations =
                travelRecommendationService.createQuickRecommendations(inquiry.getId(), inquiry.getParams());
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
                travelRecommendationService.createQuickRecommendations(inquiry.getId(), inquiry.getParams());
        assertThat(recommendations).allMatch(it -> it.getImageUrl() == null);
        travelRecommendationService.enrichWithImagesAsync(recommendations);

        var recommendationIds = recommendations.stream().map(TravelRecommendation::getId).toList();
        var updated = travelRecommendationService.findAllByIds(recommendationIds);
        assertThat(updated).hasSize(3);
        assertThat(updated).allMatch(it -> it.getImageUrl() != null);
    }

    @Test
    void parseDetailed() {
        var recommendationsRaw = Utils.toJson(DETAILED_PROMPT_OBJ);
        var parsed = TravelRecommendationService.parseDetailed(recommendationsRaw);
        assertThat(parsed).isEqualTo(DETAILED_PROMPT_OBJ);
    }

    private TravelInquiry createTravelInquiry() {
        var inquiryParams = "duration=8-14 days;from=Moscow;to=Asia;budget={from=2100, to=3800}";
        var inquiry = travelInquiryRepository.save(new TravelInquiry(null, inquiryParams, Instant.now()));
        assertThat(inquiry.getParams()).isEqualTo(inquiryParams);
        assertThat(inquiry.getId()).isNotNull();

        return inquiry;
    }
}