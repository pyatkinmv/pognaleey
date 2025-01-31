package ru.pyatkinmv.pognaleey.service;

import static ru.pyatkinmv.pognaleey.service.GptAnswerResolveHelper.parseSearchableItems;
import static ru.pyatkinmv.pognaleey.service.GptAnswerResolveHelper.splitWithPipe;
import static ru.pyatkinmv.pognaleey.service.PromptService.GUIDE_PRACTICAL_TITLES_COUNT;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import ru.pyatkinmv.pognaleey.client.GptHttpClient;
import ru.pyatkinmv.pognaleey.dto.ImageDto;
import ru.pyatkinmv.pognaleey.dto.ImageIdDto;
import ru.pyatkinmv.pognaleey.dto.SearchableItemDto;
import ru.pyatkinmv.pognaleey.model.GuideContentItemType;
import ru.pyatkinmv.pognaleey.model.ProcessingStatus;
import ru.pyatkinmv.pognaleey.model.TravelGuide;
import ru.pyatkinmv.pognaleey.model.TravelGuideContentItem;
import ru.pyatkinmv.pognaleey.repository.TravelGuideContentItemRepository;
import ru.pyatkinmv.pognaleey.repository.TravelGuideRepository;
import ru.pyatkinmv.pognaleey.util.Utils;

@Component
@Slf4j
@RequiredArgsConstructor
public class TravelGuideContentProvider {
  private final TravelInquiryService inquiryService;
  private final ExecutorService executorService;
  private final GptHttpClient gptHttpClient;
  private final TravelGuideContentItemRepository contentItemRepository;
  private final TravelGuideRepository guideRepository;
  private final PromptService promptService;
  private final MessageSource messageSource;
  private final ImageService imageService;

  static List<TravelGuideContentItem> extractItems(
      String attractionsContentResponse,
      TravelGuideContentItem attractionsFirstItem,
      Map<String, ImageDto> titleToImageMap) {
    var result = new ArrayList<TravelGuideContentItem>();

    var split = Arrays.stream(attractionsContentResponse.split("### ")).toList();

    if (split.size() <= 1) {
      throw new RuntimeException();
    }

    if (split.getFirst().contains("##")) {
      // side-effect :(
      attractionsFirstItem.setContent(split.getFirst());
      result.add(attractionsFirstItem);
    }

    for (int i = 1, ordinal = attractionsFirstItem.getOrdinal(); i < split.size(); i++) {
      var part = split.get(i);
      var topicWithBrackets = GptAnswerResolveHelper.findFirstByRegex(part, "\\{.*?\\}");

      if (topicWithBrackets.isPresent()) {
        var topic = topicWithBrackets.get().replaceAll("\\{", "").replaceAll("}", "");
        var withImageRemoved = part.replace(topicWithBrackets.get(), "");
        var partNew = String.format("### %s", withImageRemoved);
        var newMdItem =
            new TravelGuideContentItem(
                null,
                attractionsFirstItem.getGuideId(),
                partNew,
                ++ordinal,
                ProcessingStatus.IN_PROGRESS,
                GuideContentItemType.MARKDOWN);
        result.add(newMdItem);

        var image = Optional.ofNullable(titleToImageMap.get(topic));

        if (image.isPresent()) {
          var newImageItem =
              new TravelGuideContentItem(
                  null,
                  attractionsFirstItem.getGuideId(),
                  Utils.toJson(new ImageIdDto(image.get().id())),
                  ++ordinal,
                  ProcessingStatus.IN_PROGRESS,
                  GuideContentItemType.IMAGE);
          result.add(newImageItem);
        } else {
          log.warn("Couldn't find topic {} in titleToImageMap {}", topic, titleToImageMap);
        }
      }
    }

    return result;
  }

  private static GuideStructureType getType(TravelGuideContentItem item) {
    return Stream.of(GuideStructureType.values())
        .filter(it -> item.getOrdinal().equals(it.itemOrdinal))
        .findFirst()
        .orElseThrow();
  }

  private static Map<GuideStructureType, TravelGuideContentItem> buildMap(
      List<TravelGuideContentItem> items) {
    return items.stream().collect(Collectors.toMap(TravelGuideContentProvider::getType, it -> it));
  }

  private static TravelGuideContentItem getByType(
      List<TravelGuideContentItem> items, GuideStructureType type) {
    return items.stream().filter(it -> getType(it) == type).findFirst().orElseThrow();
  }

  private void enrichOrSetFailed(TravelGuideContentItem item, Runnable enrich) {
    try {
      enrich.run();
    } catch (Exception e) {
      log.error("couldn't enrich item {}; set failed. Exception: {}", item.getId(), e.getMessage());
      item.setStatus(ProcessingStatus.FAILED);
      contentItemRepository.save(item);
    }
  }

  private void enrichIntroAndConclusionOrSetFailed(
      Map<GuideStructureType, TravelGuideContentItem> typeToItemMap,
      String guideTitle,
      String inquiryParams) {
    var intro = typeToItemMap.get(GuideStructureType.INTRO);
    enrichOrSetFailed(
        intro,
        () -> {
          var introPrompt = promptService.generateGuideIntroPrompt(guideTitle, inquiryParams);
          var response = gptHttpClient.ask(introPrompt);
          intro.setContent(response);
          intro.setStatus(ProcessingStatus.READY);
          contentItemRepository.save(intro);
        });

    var conclusion = typeToItemMap.get(GuideStructureType.CONCLUSION);
    enrichOrSetFailed(
        conclusion,
        () -> {
          var introPrompt = promptService.generateGuideConclusionPrompt(guideTitle, inquiryParams);
          var response = gptHttpClient.ask(introPrompt);
          conclusion.setContent(response);
          conclusion.setStatus(ProcessingStatus.READY);
          contentItemRepository.save(conclusion);
        });
  }

  private String generateAttractionsPrompt(
      String guideTitle, String inquiryParams, List<SearchableItemDto> searchableGuideItems) {
    var titleToImagePhraseMap =
        searchableGuideItems.stream()
            .collect(Collectors.toMap(SearchableItemDto::title, SearchableItemDto::imageQuery));
    var guideVisualTopics = String.join("|", titleToImagePhraseMap.keySet());

    return promptService.generateGuideAttractionsPrompt(
        guideTitle, inquiryParams, guideVisualTopics);
  }

  private void enrichAttractionsOrSetFailed(
      TravelGuideContentItem attractionsFirst, String guideTitle, String inquiryParams) {
    enrichOrSetFailed(
        attractionsFirst,
        () -> {
          var attractionsImagesPrompt =
              promptService.generateGuideImagesPrompt(guideTitle, inquiryParams);
          var attractionsImagesResponseRaw = gptHttpClient.ask(attractionsImagesPrompt);
          var searchableGuideItems = parseSearchableItems(attractionsImagesResponseRaw);

          var imagesFuture = executorService.submit(() -> searchImages(searchableGuideItems));

          var attractionsPrompt =
              generateAttractionsPrompt(guideTitle, inquiryParams, searchableGuideItems);
          var attractionsContentResponseRaw = gptHttpClient.ask(attractionsPrompt);

          var foundImages = Utils.tryOrEmpty(imagesFuture).orElseGet(List::of);
          var foundImagesWithNotFoundStubImages =
              enrichWithNotFoundStubs(foundImages, searchableGuideItems);
          var savedImages = imageService.saveAll(foundImagesWithNotFoundStubImages);
          var titleToImageMap = Utils.toCaseInsensitiveTreeMap(savedImages, ImageDto::title);

          var contentItems =
              Optional.of(attractionsContentResponseRaw)
                  .map(it -> extractItems(it, attractionsFirst, titleToImageMap))
                  .orElse(List.of());
          contentItems.forEach(it -> it.setStatus(ProcessingStatus.READY));
          contentItemRepository.saveAll(contentItems);
        });
  }

  private List<ImageDto> enrichWithNotFoundStubs(
      List<ImageDto> foundImages, List<SearchableItemDto> searchableItems) {
    var foundImagesTitlsSet = foundImages.stream().map(ImageDto::title).collect(Collectors.toSet());
    var notFoundItems =
        searchableItems.stream().filter(it -> !foundImagesTitlsSet.contains(it.title())).toList();
    var notFoundImagesStubs =
        notFoundItems.stream()
            .map(it -> ImageDto.withoutUrls(it.title(), it.imageQuery()))
            .toList();

    return Stream.concat(foundImages.stream(), notFoundImagesStubs.stream()).toList();
  }

  void enrichGuideWithContent(
      TravelGuide guide,
      List<TravelGuideContentItem> guideContentItems,
      long inquiryId,
      String initialGuideTitle) {
    log.info("Begin enrichGuideWithContent for guide {}", guide.getId());

    try {
      var inquiry = inquiryService.findById(inquiryId);
      var inquiryParams = inquiry.getParams();
      var typeToItemMap = buildMap(guideContentItems);

      executorService.submit(
          () ->
              enrichIntroAndConclusionOrSetFailed(typeToItemMap, initialGuideTitle, inquiryParams));

      var attractions = typeToItemMap.get(GuideStructureType.ATTRACTIONS);
      executorService.submit(
          () -> enrichAttractionsOrSetFailed(attractions, initialGuideTitle, inquiryParams));

      var practicalFirstItem = typeToItemMap.get(GuideStructureType.PRACTICAL);
      var practicalTitlesPrompt =
          promptService.generateGuidePracticalTitlesPrompt(initialGuideTitle, inquiryParams);
      var practicalTitlesResponseRaw = gptHttpClient.ask(practicalTitlesPrompt);
      var titles = splitWithPipe(practicalTitlesResponseRaw);
      log.info("practical titles {}", titles);
      var generatePracticalItemsDto =
          new GeneratePracticalContentItemsDto(
              guide.getId(), initialGuideTitle, inquiryParams, titles);
      generatePracticalContentItemsOrEnrich(generatePracticalItemsDto, practicalFirstItem);

      guideRepository.updateTitle(guide.getId(), initialGuideTitle);
    } catch (Exception e) {
      log.error("Error enrichGuideWithContent", e);
    }

    log.info("end enrichGuideWithContent for guide: {}", guide.getId());
  }

  @SneakyThrows
  private void generatePracticalContentItemsOrEnrich(
      GeneratePracticalContentItemsDto dto, TravelGuideContentItem practicalFirst) {

    List<Callable<Void>> tasks = new ArrayList<>();

    for (int i = 0; i < dto.titles.size() && i < GUIDE_PRACTICAL_TITLES_COUNT; ++i) {
      var practicalTitle = dto.titles.get(i);
      var prompt = generateGuidePracticalTitlePrompt(dto, practicalTitle);

      if (i == 0) {
        tasks.add(
            () -> {
              enrichFirstAndSaveInProgress(practicalFirst, prompt);
              return null;
            });
      } else {
        int index = i;
        tasks.add(
            () -> {
              createOtherPracticalContentItems(dto, prompt, index);
              return null;
            });
      }
    }
    executorService.invokeAll(tasks);
    log.info(
        "All guide practical content items generated, set READY for practical item {}",
        practicalFirst.getId());
    practicalFirst.setStatus(ProcessingStatus.READY);
    contentItemRepository.save(practicalFirst);
  }

  private void createOtherPracticalContentItems(
      GeneratePracticalContentItemsDto dto, String prompt, int index) {
    var responseRaw = gptHttpClient.ask(prompt);

    var item =
        TravelGuideContentItem.builder()
            .content(responseRaw)
            .guideId(dto.guideId)
            .type(GuideContentItemType.MARKDOWN)
            .status(ProcessingStatus.READY)
            .ordinal(index + GuideStructureType.PRACTICAL.itemOrdinal)
            .build();
    contentItemRepository.save(item);
  }

  private void enrichFirstAndSaveInProgress(TravelGuideContentItem practicalFirst, String prompt) {
    enrichOrSetFailed(
        practicalFirst,
        () -> {
          var responseRaw = gptHttpClient.ask(prompt);
          practicalFirst.setStatus(ProcessingStatus.IN_PROGRESS);
          practicalFirst.setContent(
              String.format("%s\n\n%s", practicalFirst.getContent(), responseRaw));
          contentItemRepository.save(practicalFirst);
        });
  }

  private String generateGuidePracticalTitlePrompt(
      GeneratePracticalContentItemsDto dto, String practicalTitle) {
    var allTitles = String.join("|", dto.titles);

    return promptService.generateGuidePracticalTitlePrompt(
        dto.guideTitle, dto.inquiryParams, allTitles, practicalTitle);
  }

  List<ImageDto> searchImages(List<SearchableItemDto> searchableItems) {
    return searchableItems.stream()
        .map(it -> imageService.searchImage(it.title(), it.imageQuery()))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }

  public List<TravelGuideContentItem> createBlueprintContentItems(
      long guideId, String initialTitle, Long imageId) {
    var items =
        Stream.of(GuideStructureType.values())
            .map(
                it ->
                    TravelGuideContentItem.builder()
                        .content(initialContent(it))
                        .guideId(guideId)
                        .ordinal(it.itemOrdinal)
                        .status(it.initialStatus)
                        .type(it.type)
                        .build())
            .collect(Collectors.toCollection(ArrayList::new));
    var title = getByType(items, GuideStructureType.TITLE);
    title.setContent(initialContent(GuideStructureType.TITLE, initialTitle));
    var titleImage = getByType(items, GuideStructureType.TITLE_IMAGE);
    Optional.of(imageId).map(ImageIdDto::new).map(Utils::toJson).ifPresent(titleImage::setContent);

    return contentItemRepository.saveAllFromIterable(items);
  }

  @Nullable
  private String initialContent(GuideStructureType type, Object... objects) {
    return Optional.ofNullable(type.initialContentFormat)
        .map(
            it ->
                messageSource.getMessage(
                    it, objects, LanguageContextHolder.getLanguageLocaleOrDefault()))
        .orElse(null);
  }

  @RequiredArgsConstructor
  enum GuideStructureType {
    TITLE(0, "guide.structure.title", ProcessingStatus.READY, GuideContentItemType.MARKDOWN),
    TITLE_IMAGE(100, null, ProcessingStatus.READY, GuideContentItemType.IMAGE),
    INTRO(
        200, "guide.structure.intro", ProcessingStatus.IN_PROGRESS, GuideContentItemType.MARKDOWN),
    ATTRACTIONS(
        300,
        "guide.structure.attractions",
        ProcessingStatus.IN_PROGRESS,
        GuideContentItemType.MARKDOWN),
    PRACTICAL(
        400,
        "guide.structure.practical_info",
        ProcessingStatus.IN_PROGRESS,
        GuideContentItemType.MARKDOWN),
    CONCLUSION(
        500,
        "guide.structure.conclusion",
        ProcessingStatus.IN_PROGRESS,
        GuideContentItemType.MARKDOWN);

    private final int itemOrdinal;
    @Nullable private final String initialContentFormat;
    private final ProcessingStatus initialStatus;
    private final GuideContentItemType type;
  }

  private record GeneratePracticalContentItemsDto(
      long guideId, String guideTitle, String inquiryParams, List<String> titles) {}
}
