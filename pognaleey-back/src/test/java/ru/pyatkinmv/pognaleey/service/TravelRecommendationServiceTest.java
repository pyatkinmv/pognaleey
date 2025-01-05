package ru.pyatkinmv.pognaleey.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.pyatkinmv.pognaleey.DatabaseCleaningTest;
import ru.pyatkinmv.pognaleey.config.ClientsConfig;
import ru.pyatkinmv.pognaleey.model.TravelInquiry;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;
import ru.pyatkinmv.pognaleey.repository.TravelInquiryRepository;
import ru.pyatkinmv.pognaleey.util.Utils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.pyatkinmv.pognaleey.service.PromptService.DETAILED_PROMPT_OBJ;

@SpringBootTest
@Import(ClientsConfig.class)
class TravelRecommendationServiceTest extends DatabaseCleaningTest {
    @Autowired
    private TravelRecommendationService travelRecommendationService;
    @Autowired
    private TravelInquiryRepository travelInquiryRepository;

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