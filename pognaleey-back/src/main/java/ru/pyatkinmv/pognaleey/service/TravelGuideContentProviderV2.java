package ru.pyatkinmv.pognaleey.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import ru.pyatkinmv.pognaleey.client.GptHttpClient;
import ru.pyatkinmv.pognaleey.dto.ImageDto;
import ru.pyatkinmv.pognaleey.model.GuideContentItemType;
import ru.pyatkinmv.pognaleey.model.ProcessingStatus;
import ru.pyatkinmv.pognaleey.model.TravelGuide;
import ru.pyatkinmv.pognaleey.model.TravelGuideContentItem;
import ru.pyatkinmv.pognaleey.repository.TravelGuideContentItemRepository;
import ru.pyatkinmv.pognaleey.repository.TravelGuideRepository;
import ru.pyatkinmv.pognaleey.util.Utils;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.pyatkinmv.pognaleey.service.GptAnswerResolveHelper.parseSearchableItems;
import static ru.pyatkinmv.pognaleey.service.GptAnswerResolveHelper.splitWithPipe;
import static ru.pyatkinmv.pognaleey.service.PromptService.GUIDE_PRACTICAL_TITLES_COUNT;

@Component
@Slf4j
public class TravelGuideContentProviderV2 extends TravelGuideContentProvider {
    private final TravelInquiryService inquiryService;
    private final ExecutorService executorService;
    private final GptHttpClient gptHttpClient;
    private final TravelGuideContentItemRepository contentItemRepository;
    private final TravelGuideRepository guideRepository;
    private final TransactionTemplate transactionTemplate;
    private final PromptService promptService;
    private final MessageSource messageSource;

    public TravelGuideContentProviderV2(ImageService imageService,
                                        TravelInquiryService inquiryService, ExecutorService executorService,
                                        GptHttpClient gptHttpClient,
                                        TravelGuideContentItemRepository contentItemRepository,
                                        TravelGuideRepository guideRepository,
                                        PromptService promptService,
                                        TransactionTemplate transactionTemplate, MessageSource messageSource) {
        super(imageService);
        this.inquiryService = inquiryService;
        this.executorService = executorService;
        this.gptHttpClient = gptHttpClient;
        this.contentItemRepository = contentItemRepository;
        this.guideRepository = guideRepository;
        this.promptService = promptService;
        this.transactionTemplate = transactionTemplate;
        this.messageSource = messageSource;
    }

    private String generateSightseeingPrompt(String guideTitle,
                                             String inquiryParams,
                                             List<GptAnswerResolveHelper.SearchableItem> searchableGuideItems) {
        var titleToImagePhraseMap = searchableGuideItems.stream()
                .collect(Collectors.toMap(GptAnswerResolveHelper.SearchableItem::title, GptAnswerResolveHelper.SearchableItem::imageSearchPhrase));
        var guideVisualTopics = String.join("|", titleToImagePhraseMap.keySet());

        return promptService.generateGuideAttractionsPrompt(guideTitle, inquiryParams, guideVisualTopics);
    }

    private static GuideStructureType getType(TravelGuideContentItem item) {
        return Stream.of(GuideStructureType.values())
                .filter(it -> item.getOrdinal().equals(it.itemOrdinal))
                .findFirst()
                .orElseThrow();
    }

    private static Map<GuideStructureType, TravelGuideContentItem> buildMap(List<TravelGuideContentItem> items) {
        return items.stream()
                .collect(Collectors.toMap(TravelGuideContentProviderV2::getType, it -> it));
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

    // NOTE: Make it consequently to avoid situation when conclusion is created before intro
    private void enrichIntroAndConclusionOrSetFailed(Map<GuideStructureType, TravelGuideContentItem> typeToItemMap,
                                                     String guideTitle, String inquiryParams) {
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
        enrichOrSetFailed(conclusion,
                () -> {
                    var introPrompt = promptService.generateGuideConclusionPrompt(guideTitle, inquiryParams);
                    var response = gptHttpClient.ask(introPrompt);
                    conclusion.setContent(response);
                    conclusion.setStatus(ProcessingStatus.READY);
                    contentItemRepository.save(conclusion);
                });
    }

    static List<TravelGuideContentItem> extractItems(String sightseeingContentResponse,
                                                     TravelGuideContentItem sightseeingFirst,
                                                     Map<String, ImageDto> titleToImageMap) {
        var result = new ArrayList<TravelGuideContentItem>();

        var split = Arrays.stream(sightseeingContentResponse.split("### ")).toList();

        if (split.size() <= 1) {
            throw new RuntimeException();
        }

        if (split.getFirst().contains("##")) {
            // TODO: fix side effect
            sightseeingFirst.setContent(split.getFirst());
            result.add(sightseeingFirst);
        }

        for (int i = 1, ordinal = sightseeingFirst.getOrdinal(); i < split.size(); i++) {
            var part = split.get(i);
            var topicWithBrackets = GptAnswerResolveHelper.findFirstByRegex(part, "\\{.*?\\}");

            if (topicWithBrackets.isPresent()) {
                var topic = topicWithBrackets.get().replaceAll("\\{", "").replaceAll("}", "");
                var withImageRemoved = part.replace(topicWithBrackets.get(), "");
                var partNew = String.format("### %s", withImageRemoved);
                var newMdItem = new TravelGuideContentItem(null, sightseeingFirst.getGuideId(), partNew,
                        ++ordinal, ProcessingStatus.IN_PROGRESS, GuideContentItemType.MARKDOWN);
                result.add(newMdItem);

                if (titleToImageMap.containsKey(topic)) {
                    var newImageItem = new TravelGuideContentItem(null, sightseeingFirst.getGuideId(),
                            Utils.toJson(titleToImageMap.get(topic)),
                            ++ordinal, ProcessingStatus.IN_PROGRESS, GuideContentItemType.IMAGE);
                    result.add(newImageItem);
                } else {
                    log.warn("Couldn't find topic {} in titleToImageMap {}; just remove it", topic, titleToImageMap);
                }
            }
        }

        return result;
    }

    private static TravelGuideContentItem getByType(List<TravelGuideContentItem> items, GuideStructureType type) {
        return items.stream()
                .filter(it -> getType(it) == type)
                .findFirst()
                .orElseThrow();
    }

    private void enrichSightseeingOrSetFailed(TravelGuideContentItem sightseeingFirst, String guideTitle, String inquiryParams) {
        enrichOrSetFailed(sightseeingFirst,
                () -> {
                    var sightseeingImagesPrompt = promptService.generateGuideImagesPrompt(guideTitle, inquiryParams);
                    var sightseeingImagesResponseRaw = gptHttpClient.ask(sightseeingImagesPrompt);
                    var searchableGuideItems = parseSearchableItems(sightseeingImagesResponseRaw);

                    var imagesFuture = executorService.submit(
                            () -> searchAndSaveImages(searchableGuideItems)
                    );

                    var sightseeingPrompt = generateSightseeingPrompt(guideTitle, inquiryParams, searchableGuideItems);
                    var sightseeingContentResponseRaw = gptHttpClient.ask(sightseeingPrompt);

                    var titleToImageMap = Utils.tryOrEmpty(imagesFuture)
                            .map(it -> Utils.toCaseInsensitiveTreeMap(it, ImageDto::title))
                            .orElse(Map.of());

                    var contentItems = Optional.of(sightseeingContentResponseRaw)
                            .map(it -> extractItems(it, sightseeingFirst, titleToImageMap))
                            .orElse(List.of());
                    contentItems.forEach(it -> it.setStatus(ProcessingStatus.READY));
                    contentItemRepository.saveAll(contentItems);
                });
    }

    @Override
    void enrichGuideWithContent(TravelGuide guide, List<TravelGuideContentItem> guideContentItems, long inquiryId,
                                String guideTitle) {
        log.info("Begin enrichGuideWithContentV1 for guide {}", guide.getId());

        try {
            var inquiry = inquiryService.findById(inquiryId);
            var inquiryParams = inquiry.getParams();
            var typeToItemMap = buildMap(guideContentItems);

            executorService.submit(() -> enrichIntroAndConclusionOrSetFailed(typeToItemMap, guideTitle, inquiryParams));


            var sightseeing = typeToItemMap.get(GuideStructureType.ATTRACTIONS);
            executorService.submit(() -> enrichSightseeingOrSetFailed(sightseeing, guideTitle, inquiryParams));

            var practicalFirst = typeToItemMap.get(GuideStructureType.PRACTICAL);
            var practicalTitlesPrompt = promptService.generateGuidePracticalTitlesPrompt(guideTitle, inquiryParams);
            var practicalTitlesResponseRaw = gptHttpClient.ask(practicalTitlesPrompt);
            var titles = splitWithPipe(practicalTitlesResponseRaw);
            log.info("practical title {}", titles);

            for (var title : titles) {
                executorService.submit(
                        () -> enrichPracticalItem(practicalFirst, title, titles, guideTitle, guide.getId(), inquiryParams)
                );
            }

            transactionTemplate.executeWithoutResult(it -> {
                guideRepository.updateTitle(guide.getId(), guideTitle);
            });
        } catch (Exception e) {
            log.error("Error enrichGuideWithContent", e);
        }

        log.info("end enrichGuideWithContent for guide: {}", guide.getId());
    }

    // TODO: Refactor
    private void enrichPracticalItem(TravelGuideContentItem practicalFirst, String practicalTitle, List<String> titles, String guideTitle,
                                     long guideId, String inquiryParams) {
        var index = titles.indexOf(practicalTitle);
        var allTitles = String.join("|", titles);
        var prompt = promptService.generateGuidePracticalTitlePrompt(guideTitle, inquiryParams, allTitles, practicalTitle);

        if (index >= GUIDE_PRACTICAL_TITLES_COUNT) {
            log.error("index {} is bigger than expected", index);
            return;
        }

        if (index == 0) {
            enrichOrSetFailed(practicalFirst, () -> {
                var responseRaw = gptHttpClient.ask(prompt);
                practicalFirst.setStatus(ProcessingStatus.READY);
                practicalFirst.setContent(String.format("%s\n\n%s", practicalFirst.getContent(), responseRaw));
                contentItemRepository.save(practicalFirst);
            });
        } else {
            var responseRaw = gptHttpClient.ask(prompt);
            var item = TravelGuideContentItem.builder()
                    .content(responseRaw)
                    .guideId(guideId)
                    .type(GuideContentItemType.MARKDOWN)
                    .status(ProcessingStatus.READY)
                    .ordinal(index + GuideStructureType.PRACTICAL.itemOrdinal)
                    .build();
            contentItemRepository.save(item);
        }
    }

    // TODO: Refactor
    @Override
    public List<TravelGuideContentItem> createBlueprintContentItems(long guideId, String initialTitle,
                                                                    @Nullable Long imageId) {
        var items = Stream.of(GuideStructureType.values())
                .map(it -> TravelGuideContentItem.builder()
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
        var imageJson = Optional.ofNullable(imageId).map(imageService::findByIdOrThrow)
                .map(Utils::toJson);
        if (imageJson.isPresent()) {
            titleImage.setContent(imageJson.get());
        } else {
            items.remove(titleImage);
        }

        return contentItemRepository.saveAllFromIterable(items);
    }

    @Nullable
    private String initialContent(GuideStructureType type, Object... objects) {
        return Optional.ofNullable(type.initialContentFormat)
                .map(it -> messageSource.getMessage(it, objects, LanguageContextHolder.getLanguageLocaleOrDefault()))
                .orElse(null);
    }

    @RequiredArgsConstructor
    public enum GuideStructureType {
        TITLE(0, "guide.structure.title", ProcessingStatus.READY, GuideContentItemType.MARKDOWN),
        TITLE_IMAGE(10, null, ProcessingStatus.READY, GuideContentItemType.IMAGE),
        INTRO(20, "guide.structure.intro", ProcessingStatus.IN_PROGRESS, GuideContentItemType.MARKDOWN),
        ATTRACTIONS(30, "guide.structure.attractions", ProcessingStatus.IN_PROGRESS, GuideContentItemType.MARKDOWN),
        PRACTICAL(40, "guide.structure.practical_info", ProcessingStatus.IN_PROGRESS, GuideContentItemType.MARKDOWN),
        CONCLUSION(50, "guide.structure.conclusion", ProcessingStatus.IN_PROGRESS, GuideContentItemType.MARKDOWN);

        private final int itemOrdinal;
        @Nullable
        private final String initialContentFormat;
        private final ProcessingStatus initialStatus;
        private final GuideContentItemType type;
    }
}
