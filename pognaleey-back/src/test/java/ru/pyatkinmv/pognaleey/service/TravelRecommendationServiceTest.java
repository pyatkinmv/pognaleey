package ru.pyatkinmv.pognaleey.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.pyatkinmv.pognaleey.DatabaseCleaningTest;
import ru.pyatkinmv.pognaleey.dto.TravelRecommendationDto;
import ru.pyatkinmv.pognaleey.model.TravelInquiry;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;
import ru.pyatkinmv.pognaleey.model.TravelRecommendationStatus;
import ru.pyatkinmv.pognaleey.repository.TravelInquiryRepository;
import ru.pyatkinmv.pognaleey.repository.TravelRecommendationRepository;
import ru.pyatkinmv.pognaleey.util.Utils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.pyatkinmv.pognaleey.service.PromptService.DETAILED_PROMPT_OBJ;

@SpringBootTest
class TravelRecommendationServiceTest extends DatabaseCleaningTest {
    @Autowired
    private TravelRecommendationService recommendationService;
    @Autowired
    private TravelRecommendationRepository recommendationRepository;
    @Autowired
    private TravelInquiryRepository travelInquiryRepository;

    @Test
    void enrichWithShortInfo() {
        var inquiry = createTravelInquiryAndBlueprintRecommendations();
        var blueprintRecommendations = recommendationRepository.findByInquiryId(inquiry.getId());
        var recommendations =
                recommendationService.enrichWithShortInfo(blueprintRecommendations, inquiry.getParams());
        assertThat(recommendations)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id", "createdAt")
                .containsExactly(
                        new TravelRecommendation(null, null, inquiry.getId(), "Грузия, Тбилиси и винные регионы", "Грузия весна пейзаж", null, null, TravelRecommendationStatus.IN_PROGRESS),
                        new TravelRecommendation(null, null, inquiry.getId(), "Париж, Франция: Город любви", "Париж Эйфелева башня закат", null, null, TravelRecommendationStatus.IN_PROGRESS),
                        new TravelRecommendation(null, null, inquiry.getId(), "Красная Поляна, Сочи: Горнолыжный отдых", "Красная Поляна лыжи снег", null, null, TravelRecommendationStatus.IN_PROGRESS),
                        new TravelRecommendation(null, null, inquiry.getId(), "Индонезия, Бали: Пляжный отдых", "Бали пляжи закат", null, null, TravelRecommendationStatus.IN_PROGRESS),
                        new TravelRecommendation(null, null, inquiry.getId(), "Исландия: Природные чудеса", "Исландия водопады ледники", null, null, TravelRecommendationStatus.IN_PROGRESS)
                );
    }

    @Test
    void enrichWithDetailsAsync() {
        var inquiry = createTravelInquiryAndBlueprintRecommendations();
        var blueprintRecommendations = recommendationRepository.findByInquiryId(inquiry.getId());
        var recommendations =
                recommendationService.enrichWithShortInfo(blueprintRecommendations, inquiry.getParams());
        assertThat(recommendations).allMatch(it -> it.getDetails() == null);
        recommendationService.enrichWithDetailsAsyncEach(recommendations, inquiry.getParams());

        var recommendationIds = recommendations.stream().map(TravelRecommendation::getId).toList();
        var updated = recommendationService.getRecommendations(recommendationIds);
        assertThat(updated.recommendations()).hasSize(5);
        assertThat(updated.recommendations()).allMatch(it -> it.details() != null);
    }

    @Test
    void enrichWithImagesAsync() {
        var inquiry = createTravelInquiryAndBlueprintRecommendations();
        var blueprintRecommendations = recommendationRepository.findByInquiryId(inquiry.getId());
        var recommendations =
                recommendationService.enrichWithShortInfo(blueprintRecommendations, inquiry.getParams());
        assertThat(recommendations).allMatch(it -> it.getImageUrl() == null);
        recommendationService.enrichWithImagesAsyncAll(recommendations);

        var recommendationIds = recommendations.stream().map(TravelRecommendation::getId).toList();
        var updated = recommendationService.getRecommendations(recommendationIds);
        assertThat(updated.recommendations()).hasSize(5);
        assertThat(updated.recommendations()).allMatch(it -> it.image() != null);
    }

    @Test
    void parseDetailed() {
        var recommendationsRaw = Utils.toJson(DETAILED_PROMPT_OBJ);
        var parsed = TravelRecommendationService.parseDetailed(recommendationsRaw);
        assertThat(parsed).isEqualTo(DETAILED_PROMPT_OBJ);
    }

    @Test
    void getRecommendationsWithoutEnrichment() {
        var inquiry = createTravelInquiryAndBlueprintRecommendations();
        var blueprintRecommendations = recommendationRepository.findByInquiryId(inquiry.getId());
        recommendationService.enrichWithShortInfo(blueprintRecommendations, inquiry.getParams());
        var recommendations = recommendationService.getRecommendations(inquiry.getId());
        assertThat(recommendations.recommendations()).allMatch(
                it -> it.id() != 0
                        && it.title() != null
                        && it.details() == null
                        && it.image() == null
                        && it.guideId() == null
                        && it.status().equals("IN_PROGRESS")
        );
        var recommendationsIds = recommendations.recommendations().stream().map(TravelRecommendationDto::id).toList();
        var recommendationsByIds = recommendationService.getRecommendations(recommendationsIds);
        assertThat(recommendations).isEqualTo(recommendationsByIds);
    }

    private TravelInquiry createTravelInquiryAndBlueprintRecommendations() {
        var inquiryParams = "duration=8-14 days;from=Moscow;to=Asia;budget={from=2100, to=3800}";
        var inquiry = travelInquiryRepository.save(new TravelInquiry(null, inquiryParams, Instant.now(), null));
        assertThat(inquiry.getParams()).isEqualTo(inquiryParams);
        assertThat(inquiry.getId()).isNotNull();
        recommendationService.createBlueprintRecommendations(inquiry.getId());

        return inquiry;
    }
}