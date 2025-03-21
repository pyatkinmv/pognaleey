package ru.pyatkinmv.pognaleey.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.pyatkinmv.pognaleey.DatabaseCleaningTest;
import ru.pyatkinmv.pognaleey.dto.GptResponseRecommendationDetailsDto;
import ru.pyatkinmv.pognaleey.dto.ImageDto;
import ru.pyatkinmv.pognaleey.dto.TravelRecommendationDto;
import ru.pyatkinmv.pognaleey.model.ProcessingStatus;
import ru.pyatkinmv.pognaleey.model.TravelInquiry;
import ru.pyatkinmv.pognaleey.repository.TravelInquiryRepository;
import ru.pyatkinmv.pognaleey.repository.TravelRecommendationRepository;
import ru.pyatkinmv.pognaleey.util.Utils;

@SpringBootTest
class TravelRecommendationServiceTest extends DatabaseCleaningTest {
  @Autowired private TravelRecommendationService recommendationService;
  @Autowired private TravelRecommendationRepository recommendationRepository;
  @Autowired private TravelInquiryRepository travelInquiryRepository;
  @Autowired private PromptService promptService;

  @Test
  void enrichRecommendationsAsync() {
    var inquiry = createTravelInquiryAndBlueprintRecommendations();
    var blueprintRecommendations = recommendationRepository.findByInquiryId(inquiry.getId());
    recommendationService.enrichRecommendationsAsync(blueprintRecommendations, inquiry.getParams());
    var recommendations =
        recommendationService.getRecommendations(inquiry.getId()).recommendations();
    assertThat(recommendations).allMatch(it -> it.details() != null);
    assertThat(recommendations)
        .usingRecursiveComparison()
        .ignoringFields("id", "createdAt", "details", "image.id")
        .isEqualTo(
            List.of(
                new TravelRecommendationDto(
                    -1L,
                    "Грузия, Тбилиси и винные регионы",
                    ProcessingStatus.READY.name(),
                    null,
                    new ImageDto(
                        "Грузия, Тбилиси и винные регионы",
                        "imageUrl",
                        "thumbnailUrl",
                        "Грузия весна пейзаж"),
                    null),
                new TravelRecommendationDto(
                    -1L,
                    "Париж, Франция: Город любви",
                    ProcessingStatus.READY.name(),
                    null,
                    new ImageDto(
                        "Париж, Франция: Город любви",
                        "imageUrl",
                        "thumbnailUrl",
                        "Париж Эйфелева башня закат"),
                    null),
                new TravelRecommendationDto(
                    -1L,
                    "Красная Поляна, Сочи: Горнолыжный отдых",
                    ProcessingStatus.READY.name(),
                    null,
                    new ImageDto(
                        "Красная Поляна, Сочи: Горнолыжный отдых",
                        "imageUrl",
                        "thumbnailUrl",
                        "Красная Поляна лыжи снег"),
                    null),
                new TravelRecommendationDto(
                    -1L,
                    "Индонезия, Бали: Пляжный отдых",
                    ProcessingStatus.READY.name(),
                    null,
                    new ImageDto(
                        "Индонезия, Бали: Пляжный отдых",
                        "imageUrl",
                        "thumbnailUrl",
                        "Бали пляжи закат"),
                    null),
                new TravelRecommendationDto(
                    -1L,
                    "Исландия: Природные чудеса",
                    ProcessingStatus.READY.name(),
                    null,
                    new ImageDto(
                        "Исландия: Природные чудеса",
                        "imageUrl",
                        "thumbnailUrl",
                        "Исландия водопады ледники"),
                    null)));
  }

  @Test
  void parseDetailed() {
    var recommendationsRaw = promptService.buildExampleResponseRecommendationDetailsDto();
    var parsed = TravelRecommendationService.parseDetailed(recommendationsRaw);
    assertThat(parsed)
        .isEqualTo(Utils.toObject(recommendationsRaw, GptResponseRecommendationDetailsDto.class));
  }

  @Test
  void getRecommendationsWithoutEnrichment() {
    var inquiry = createTravelInquiryAndBlueprintRecommendations();
    var recommendations = recommendationService.getRecommendations(inquiry.getId());
    assertThat(recommendations.recommendations())
        .allMatch(
            it ->
                it.id() > 0
                    && it.title() == null
                    && it.details() == null
                    && it.image() == null
                    && it.guideId() == null
                    && it.status().equals(ProcessingStatus.IN_PROGRESS.name()));
    var recommendationsIds =
        recommendations.recommendations().stream().map(TravelRecommendationDto::id).toList();
    var recommendationsByIds = recommendationService.getRecommendations(recommendationsIds);
    assertThat(recommendations).isEqualTo(recommendationsByIds);
  }

  private TravelInquiry createTravelInquiryAndBlueprintRecommendations() {
    var inquiryParams = "duration=8-14 days;from=Moscow;to=Asia;budget={from=2100, to=3800}";
    var inquiry =
        travelInquiryRepository.save(new TravelInquiry(null, inquiryParams, Instant.now(), null));
    assertThat(inquiry.getParams()).isEqualTo(inquiryParams);
    assertThat(inquiry.getId()).isNotNull();
    recommendationService.createBlueprintRecommendations(inquiry.getId());

    return inquiry;
  }
}
