package ru.pyatkinmv.pognaleey.service;

import static ru.pyatkinmv.pognaleey.service.GptAnswerResolveHelper.*;
import static ru.pyatkinmv.pognaleey.util.Utils.getOrEmpty;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.pyatkinmv.pognaleey.client.GptHttpClient;
import ru.pyatkinmv.pognaleey.dto.GptResponseRecommendationDetailsDto;
import ru.pyatkinmv.pognaleey.dto.ImageDto;
import ru.pyatkinmv.pognaleey.dto.TravelRecommendationListDto;
import ru.pyatkinmv.pognaleey.mapper.TravelMapper;
import ru.pyatkinmv.pognaleey.model.ProcessingStatus;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;
import ru.pyatkinmv.pognaleey.repository.TravelGuideRepository;
import ru.pyatkinmv.pognaleey.repository.TravelInquiryRepository;
import ru.pyatkinmv.pognaleey.repository.TravelRecommendationRepository;
import ru.pyatkinmv.pognaleey.util.Utils;

@Slf4j
@Service
@RequiredArgsConstructor
public class TravelRecommendationService {
  public static final int RECOMMENDATIONS_NUMBER = 5;

  private final GptHttpClient gptHttpClient;
  private final ImageService imageService;
  private final TravelRecommendationRepository recommendationRepository;
  private final TravelInquiryRepository inquiryRepository;
  private final TravelGuideRepository guideRepository;
  private final ExecutorService executorService;
  private final PromptService promptService;

  private static List<TitleAndImageQuery> parseQuick(String quickRecommendationsAnswer) {
    var parsed = parseSearchableItems(quickRecommendationsAnswer);

    return parsed.stream()
        .map(it -> new TitleAndImageQuery(it.title().trim(), it.imageSearchPhrase().trim()))
        .toList();
  }

  @SneakyThrows
  static GptResponseRecommendationDetailsDto parseDetailed(String gptRecommendationsAnswerRaw) {
    gptRecommendationsAnswerRaw = removeJsonTagsIfPresent(gptRecommendationsAnswerRaw);
    var regex = "\\{[\\s\\S]*\"description\":[\\s\\S]*}";
    var pattern = Pattern.compile(regex);
    var matcher = pattern.matcher(gptRecommendationsAnswerRaw);

    if (matcher.find()) {
      var jsonStr = matcher.group(0);

      return Utils.toObject(jsonStr, GptResponseRecommendationDetailsDto.class);
    } else {
      throw new RuntimeException(
          "gptRecommendationsAnswerRaw has wrong format: " + gptRecommendationsAnswerRaw);
    }
  }

  static TravelRecommendation enrichWithTitleOrSetFailed(
      TravelRecommendation recommendation, Optional<TitleAndImageQuery> shortInfo) {
    if (shortInfo.isPresent()
        && shortInfo.get().imageQuery() != null
        && shortInfo.get().title() != null) {
      recommendation.setTitle(shortInfo.get().title());
      recommendation.setStatus(ProcessingStatus.IN_PROGRESS);
    } else {
      recommendation.setStatus(ProcessingStatus.FAILED);
    }

    return recommendation;
  }

  @Async
  public void enrichRecommendationsAsync(
      List<TravelRecommendation> blueprintRecommendations, String inquiryParams) {
    var inquiryId =
        blueprintRecommendations.stream()
            .map(TravelRecommendation::getInquiryId)
            .findFirst()
            .orElseThrow();
    log.info("begin createRecommendationsAsync for inquiryId {}", inquiryId);

    List<TravelRecommendation> updated;
    List<TitleAndImageQuery> titleAndImageQueries;

    try {
      var prompt = promptService.generateQuickPrompt(RECOMMENDATIONS_NUMBER, inquiryParams);
      var answer = gptHttpClient.ask(prompt);
      var titleAndImageQueriesParsed = parseQuick(answer);
      titleAndImageQueries =
          resolveInCaseGeneratedMoreOrLessThanExpected(titleAndImageQueriesParsed);

      var recs =
          IntStream.range(0, blueprintRecommendations.size())
              .mapToObj(
                  i ->
                      enrichWithTitleOrSetFailed(
                          blueprintRecommendations.get(i), getOrEmpty(titleAndImageQueries, i)))
              .toList();
      log.info("save recommendations {}", recs);

      updated = recommendationRepository.saveAllFromIterable(recs);
    } catch (Exception e) {
      var recommendationIds =
          Utils.extracting(blueprintRecommendations, TravelRecommendation::getId);
      log.error("enrichRecommendationsAsync error: {}, set failed for: {}", e, recommendationIds);
      recommendationRepository.setFailed(recommendationIds);

      return;
    }

    // TODO: Нет гарантий, что imageQuery != null
    var withShortInfo =
        updated.stream().filter(it -> it.getStatus() != ProcessingStatus.FAILED).toList();

    enrichWithDetailsAsyncEach(withShortInfo, inquiryParams);
    enrichWithImagesAsyncEach(withShortInfo, titleAndImageQueries);
  }

  @SneakyThrows
  void enrichWithDetailsAsyncEach(
      List<TravelRecommendation> recommendations, String inquiryParams) {
    log.info("begin enrichWithDetailsAsyncEach for recommendations {}", recommendations);
    recommendations.forEach(
        it -> executorService.execute(() -> enrichWithDetails(it, inquiryParams)));
  }

  void enrichWithImagesAsyncEach(
      List<TravelRecommendation> recommendations, List<TitleAndImageQuery> titleAndImageQueries) {
    var titleToImageQuery =
        titleAndImageQueries.stream()
            .collect(Collectors.toMap(TitleAndImageQuery::title, TitleAndImageQuery::imageQuery));
    log.info("begin enrichWithImagesAsyncAll for recommendations {}", recommendations);
    recommendations.forEach(
        it ->
            executorService.execute(
                () -> searchAndSaveImageAndUpdateStatus(it, titleToImageQuery.get(it.getTitle()))));
  }

  void enrichWithImages(
      List<TravelRecommendation> recommendations, List<TitleAndImageQuery> titleAndImageQueries) {
    var titleToImageQuery =
        titleAndImageQueries.stream()
            .collect(Collectors.toMap(TitleAndImageQuery::title, TitleAndImageQuery::imageQuery));
    log.info("begin enrichWithImages for recommendations {}", recommendations);
    recommendations.forEach(
        it -> searchAndSaveImageAndUpdateStatus(it, titleToImageQuery.get(it.getTitle())));
  }

  private void enrichWithDetails(TravelRecommendation recommendation, String inquiryParams) {
    log.info("begin enrichWithDetails for recommendation: {}", recommendation.getId());

    try {
      var prompt = promptService.generateDetailedPrompt(recommendation, inquiryParams);
      var recommendationDetailsRaw = gptHttpClient.ask(prompt);
      var details = parseDetailed(recommendationDetailsRaw);
      var detailsJson = Utils.toJson(details);
      log.info("update recommendation {} with details {}", recommendation.getId(), detailsJson);
      recommendationRepository.updateDetailsAndStatus(recommendation.getId(), detailsJson);
    } catch (Exception e) {
      log.error(
          "Can not enrichWithDetails for recommendation {}, set failed status; reason: ",
          recommendation.getId(),
          e);
      recommendationRepository.setStatus(recommendation.getId(), ProcessingStatus.FAILED.name());
    }

    log.info("end enrichWithDetails for recommendation: {}", recommendation.getId());
  }

  List<TravelRecommendation> findByInquiryId(Long inquiryId) {
    return recommendationRepository.findByInquiryId(inquiryId);
  }

  @SneakyThrows
  private void searchAndSaveImageAndUpdateStatus(
      TravelRecommendation recommendation, String imageQuery) {
    try {
      var image =
          imageService
              .searchImage(recommendation.getTitle(), imageQuery)
              .orElseGet(() -> ImageDto.withoutUrls(recommendation.getTitle(), imageQuery));

      if (image.isWithoutUrls()) {
        log.warn(
            "Not found image url for recommendation {}, searchQuery {}",
            recommendation.getId(),
            imageQuery);
      }

      var imageId = imageService.saveImage(image).id();
      recommendationRepository.updateImageIdAndStatus(recommendation.getId(), imageId);
    } catch (Exception e) {
      log.error(
          "Couldn't searchAndSaveImageAndUpdateStatus for recommendation {}, set failed status",
          recommendation.getId(),
          e);
      recommendationRepository.setStatus(recommendation.getId(), ProcessingStatus.FAILED.name());
    }
  }

  public TravelRecommendation findById(long recommendationId) {
    return recommendationRepository.findById(recommendationId).orElseThrow();
  }

  public TravelRecommendationListDto getRecommendations(List<Long> recommendationIds) {
    var recommendations = recommendationRepository.findAllByIdIn(recommendationIds);
    var recommendationsIds = recommendations.stream().map(TravelRecommendation::getId).toList();
    var recommendationIdToGuideIdMap = getRecommendationToGuideMap(recommendationsIds);
    var idToImageMap = getIdToImageMap(recommendations);

    return TravelMapper.toRecommendationListDto(
        recommendations, idToImageMap, recommendationIdToGuideIdMap);
  }

  private Map<Long, Long> getRecommendationToGuideMap(List<Long> recommendationIds) {
    if (recommendationIds.isEmpty()) {
      return Collections.emptyMap();
    } else {
      return guideRepository.getRecommendationToGuideMap(recommendationIds);
    }
  }

  public TravelRecommendationListDto getRecommendations(long inquiryId) {
    var inquiry = inquiryRepository.findById(inquiryId);

    if (inquiry.isEmpty()) {
      throw new RuntimeException("Inquiry with id " + inquiryId + " does not exist");
    }

    var recommendations = findByInquiryId(inquiryId);

    if (recommendations.isEmpty()) {
      return TravelMapper.toRecommendationListDto(
          Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap());
    }

    var recommendationsIds = recommendations.stream().map(TravelRecommendation::getId).toList();
    var recommendationIdToGuideIdMap = getRecommendationToGuideMap(recommendationsIds);
    var idToImageMap = getIdToImageMap(recommendations);

    return TravelMapper.toRecommendationListDto(
        recommendations, idToImageMap, recommendationIdToGuideIdMap);
  }

  private Map<Long, ImageDto> getIdToImageMap(List<TravelRecommendation> recommendations) {
    var imagesIds =
        recommendations.stream()
            .map(TravelRecommendation::getImageId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    return imageService.getIdToImageMap(imagesIds);
  }

  public List<TravelRecommendation> createBlueprintRecommendations(Long inquiryId) {
    var recommendations =
        IntStream.range(0, RECOMMENDATIONS_NUMBER)
            .mapToObj(
                i ->
                    TravelRecommendation.builder()
                        .inquiryId(inquiryId)
                        .createdAt(Instant.now())
                        .status(ProcessingStatus.IN_PROGRESS)
                        .build())
            .toList();

    return recommendationRepository.saveAllFromIterable(recommendations);
  }

  record TitleAndImageQuery(String title, String imageQuery) {}
}
