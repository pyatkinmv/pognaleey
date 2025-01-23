package ru.pyatkinmv.pognaleey.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import ru.pyatkinmv.pognaleey.client.GptHttpClient;
import ru.pyatkinmv.pognaleey.dto.ImageDto;
import ru.pyatkinmv.pognaleey.model.ProcessingStatus;
import ru.pyatkinmv.pognaleey.model.TravelGuide;
import ru.pyatkinmv.pognaleey.model.TravelGuideContentItem;
import ru.pyatkinmv.pognaleey.repository.TravelGuideContentItemRepository;
import ru.pyatkinmv.pognaleey.repository.TravelGuideRepository;
import ru.pyatkinmv.pognaleey.util.Utils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.pyatkinmv.pognaleey.service.GptAnswerResolveHelper.*;
import static ru.pyatkinmv.pognaleey.service.PromptService.*;

@Component
@Slf4j
public class TravelGuideContentProviderV2 extends TravelGuideContentProvider {
    private final TravelInquiryService inquiryService;
    private final ExecutorService executorService;
    private final GptHttpClient gptHttpClient;
    private final TravelGuideContentItemRepository contentItemRepository;
    private final TravelGuideRepository guideRepository;
    private final TransactionTemplate transactionTemplate;

    public TravelGuideContentProviderV2(ImageService imageService,
                                        TravelInquiryService inquiryService, ExecutorService executorService,
                                        GptHttpClient gptHttpClient,
                                        TravelGuideContentItemRepository contentItemRepository,
                                        TravelGuideRepository guideRepository,
                                        TransactionTemplate transactionTemplate) {
        super(imageService);
        this.inquiryService = inquiryService;
        this.executorService = executorService;
        this.gptHttpClient = gptHttpClient;
        this.contentItemRepository = contentItemRepository;
        this.guideRepository = guideRepository;
        this.transactionTemplate = transactionTemplate;
    }

    private static String generateSightseeingPrompt(String guideTitle,
                                                    String inquiryParams,
                                                    List<GptAnswerResolveHelper.SearchableItem> searchableGuideItems) {
        var titleToImagePhraseMap = searchableGuideItems.stream()
                .collect(Collectors.toMap(GptAnswerResolveHelper.SearchableItem::title, GptAnswerResolveHelper.SearchableItem::imageSearchPhrase));
        var guideVisualTopics = String.join("|", titleToImagePhraseMap.keySet());

        return PromptService.generateGuideVisualPrompt(guideTitle, inquiryParams, guideVisualTopics);
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
                    var introPrompt = generateGuideIntroPrompt(guideTitle, inquiryParams);
                    var response = gptHttpClient.ask(introPrompt);
                    intro.setContent(response);
                    intro.setStatus(ProcessingStatus.READY);
                    contentItemRepository.save(intro);
                });

        var conclusion = typeToItemMap.get(GuideStructureType.CONCLUSION);
        enrichOrSetFailed(conclusion,
                () -> {
                    var introPrompt = generateGuideConclusionPrompt(guideTitle, inquiryParams);
                    var response = gptHttpClient.ask(introPrompt);
                    conclusion.setContent(response);
                    conclusion.setStatus(ProcessingStatus.READY);
                    contentItemRepository.save(conclusion);
                });
    }

    private void enrichSightseeingOrSetFailed(TravelGuideContentItem sightseeing, String guideTitle, String inquiryParams) {
        enrichOrSetFailed(sightseeing,
                () -> {
                    var sightseeingImagesPrompt = PromptService.generateGuideImagesPrompt(guideTitle, inquiryParams);
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

                    var sightseeingContent = Optional.of(sightseeingContentResponseRaw)
                            .map(it -> enrichWithContentImages(it, titleToImageMap))
                            .map(GptAnswerResolveHelper::stripCurlyBraces)
                            .orElseThrow();
                    sightseeing.setContent(sightseeingContent);
                    sightseeing.setStatus(ProcessingStatus.READY);
                    contentItemRepository.save(sightseeing);
                });
    }

    // TODO: Refactor
    private void enrichPracticalItem(TravelGuideContentItem practicalFirst, String practicalTitle, List<String> titles, String guideTitle,
                                     long guideId, String inquiryParams) {
        var index = titles.indexOf(practicalTitle);
        var allTitles = String.join("|", titles);
        var prompt = PromptService.generateGuidePracticalTitlePrompt(guideTitle, inquiryParams, allTitles, practicalTitle);

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
                    .status(ProcessingStatus.READY)
                    .ordinal(index + GuideStructureType.PRACTICAL.itemOrdinal)
                    .build();
            contentItemRepository.save(item);
        }
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


            var sightseeing = typeToItemMap.get(GuideStructureType.SIGHTSEEING);
            executorService.submit(() -> enrichSightseeingOrSetFailed(sightseeing, guideTitle, inquiryParams));

            var practicalFirst = typeToItemMap.get(GuideStructureType.PRACTICAL);
            var practicalTitlesPrompt = PromptService.generateGuidePracticalTitlesPrompt(guideTitle, inquiryParams);
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

    @Override
    public List<TravelGuideContentItem> createBlueprintContentItems(long guideId, String initialTitle, Long imageId) {
        var items = Stream.of(GuideStructureType.values())
                .map(it -> TravelGuideContentItem.builder()
                        .content(it.initialContent)
                        .guideId(guideId)
                        .ordinal(it.itemOrdinal)
                        .status(it.initialStatus)
                        .build())
                .toList();
        var image = imageService.findByIdOrThrow(imageId);
        var imageStr = String.format(MARKDOWN_IMAGE_FORMAT, image.url(), replaceQuotes(initialTitle));
        var titleWithImage = items.stream()
                .filter(it -> it.getOrdinal().equals(GuideStructureType.TITLE_WITH_IMAGE.itemOrdinal))
                .findFirst()
                .orElseThrow();
        titleWithImage.setContent(String.format("# %s\n%s\n\n", initialTitle, imageStr));

        return contentItemRepository.saveAllFromIterable(items);
    }

    @RequiredArgsConstructor
    enum GuideStructureType {
        TITLE_WITH_IMAGE(1, null, ProcessingStatus.READY, false),
        INTRO(2, "## Введение\n\n", ProcessingStatus.IN_PROGRESS, true),
        SIGHTSEEING(3, "## Достопримечательности, маршруты, развлечения\n\n", ProcessingStatus.IN_PROGRESS, true),
        PRACTICAL(4, "## Практическая информация\n\n", ProcessingStatus.IN_PROGRESS, false),
        // Another titles generated in process
        CONCLUSION(4 + GUIDE_PRACTICAL_TITLES_COUNT, "## Заключение\n\n", ProcessingStatus.IN_PROGRESS, true);

        private final int itemOrdinal;
        @Nullable
        private final String initialContent;
        private final ProcessingStatus initialStatus;
        private final boolean isOverridable;
    }
}
