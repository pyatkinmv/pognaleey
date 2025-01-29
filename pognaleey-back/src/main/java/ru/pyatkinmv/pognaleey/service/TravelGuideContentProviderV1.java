package ru.pyatkinmv.pognaleey.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static ru.pyatkinmv.pognaleey.service.GptAnswerResolveHelper.parseSearchableItems;
import static ru.pyatkinmv.pognaleey.service.GptAnswerResolveHelper.replaceQuotes;

@Component
@Slf4j
@Deprecated
public class TravelGuideContentProviderV1 extends TravelGuideContentProvider {
    private static final String MARKDOWN_IMAGE_FORMAT =
            "<img src=\"%s\" alt=\"%s\" style=\"width: 55rem; display: block; margin: 0 auto;\">";

    private final TravelInquiryService inquiryService;
    private final ExecutorService executorService;
    private final GptHttpClient gptHttpClient;
    private final TravelGuideContentItemRepository contentItemRepository;
    private final TravelGuideRepository guideRepository;
    private final PromptService promptService;
    private final TransactionTemplate transactionTemplate;

    public TravelGuideContentProviderV1(ImageService imageService,
                                        TravelInquiryService inquiryService, ExecutorService executorService,
                                        GptHttpClient gptHttpClient,
                                        TravelGuideContentItemRepository contentItemRepository,
                                        TravelGuideRepository guideRepository,
                                        PromptService promptService,
                                        TransactionTemplate transactionTemplate) {
        super(imageService);
        this.inquiryService = inquiryService;
        this.executorService = executorService;
        this.gptHttpClient = gptHttpClient;
        this.contentItemRepository = contentItemRepository;
        this.guideRepository = guideRepository;
        this.promptService = promptService;
        this.transactionTemplate = transactionTemplate;
    }

    private static String enrichGuideWithTitleImage(String guideContent, @Nullable String title,
                                                    String titleImageUrl) {
        if (title == null) {
            return guideContent;
        }

        var titleWithBrackets = String.format("{%s}", title);
        var target = String.format("%s\n" + MARKDOWN_IMAGE_FORMAT, title, titleImageUrl, title);

        if (guideContent.startsWith(titleWithBrackets)) {
            target = "# " + target;
        }

        var result = guideContent.replace(titleWithBrackets, target);
        log.info("Resolved content for title image: {}...", result.substring(0, 100));

        return result;
    }

    private String generateGuidePrompt(String guideTitle,
                                       String inquiryParams,
                                       List<GptAnswerResolveHelper.SearchableItem> searchableGuideItems) {
        var titleToImagePhraseMap = searchableGuideItems.stream()
                .collect(Collectors.toMap(GptAnswerResolveHelper.SearchableItem::title, GptAnswerResolveHelper.SearchableItem::imageSearchPhrase));
        var guideTopics = String.join("|", titleToImagePhraseMap.keySet());

        return promptService.generateCreateGuidePrompt(guideTitle, inquiryParams, guideTopics);
    }

    /**
     * Extracts the title from the guide content by removing the curly braces `{}`
     * around the first substring found. If no such substring is found, logs a warning
     * and returns `null`.
     *
     * @param guideContent The guide content to extract the title from.
     * @return The title without curly braces, or `null` if no title is found.
     *
     * <p>Example usage:</p>
     * {@code resolveGuideTitle("# {Guide Title} text");} returns {@code "Guide Title"}.
     * {@code resolveGuideTitle("No braces here");} returns null.
     */
    @Nullable
    private static String resolveGuideTitle(String guideContent) {
        var regex = "\\{.*?\\}";
        var titleWithBracketsOpt = GptAnswerResolveHelper.findFirstByRegex(guideContent, regex);

        if (titleWithBracketsOpt.isEmpty()) {
            log.warn("Could not find title for guide: {}...", guideContent.substring(0, 100));

            return null;
        }

        var titleWithBrackets = titleWithBracketsOpt.get();
        var titleWithoutBraces = titleWithBrackets.replaceAll("\\{", "").replaceAll("}", "");

        return GptAnswerResolveHelper.replaceQuotes(titleWithoutBraces);
    }

    @Override
    @SneakyThrows
    void enrichGuideWithContent(TravelGuide guide, List<TravelGuideContentItem> guideContentItems, long inquiryId,
                                String recommendationTitle) {
        log.info("Begin enrichGuideWithContent for guide {}", guide.getId());

        try {
            var inquiry = inquiryService.findById(inquiryId);
            var guideImagesPrompt = promptService.generateGuideImagesPrompt(recommendationTitle, inquiry.getParams());
            var imagesGuideResponseRaw = gptHttpClient.ask(guideImagesPrompt);
            var searchableGuideItems = parseSearchableItems(imagesGuideResponseRaw);

            var result = executorService.submit(() -> searchImages(searchableGuideItems));

            var createGuidePrompt = generateGuidePrompt(recommendationTitle, inquiry.getParams(), searchableGuideItems);
            var guideContentRaw = gptHttpClient.ask(createGuidePrompt);
            var guideContentTitle = resolveGuideTitle(guideContentRaw);

            var titleToImageMap = Utils.tryOrEmpty(result)
                    .map(it -> Utils.toCaseInsensitiveTreeMap(it, ImageDto::title))
                    .orElse(Map.of());

            var image = imageService.findByIdOrThrow(guide.getId());
            var guideContent = Optional.of(guideContentRaw)
                    .map(it -> enrichWithContentImages(guideContentRaw, titleToImageMap))
                    .map(it -> enrichGuideWithTitleImage(it, guideContentTitle, image.url()))
                    .map(GptAnswerResolveHelper::stripCurlyBraces)
//                .map(it -> Utils.peek(() -> Utils.writeFile(it, guide.getId()), it))
                    .orElseThrow();

            var guideTitle = Optional.ofNullable(guideContentTitle).orElse(recommendationTitle);
            var guideContentItem = guideContentItems.getFirst();
            guideContentItem.setContent(guideContent);
            guideContentItem.setStatus(ProcessingStatus.READY);

            transactionTemplate.executeWithoutResult(it -> {
                guideRepository.updateTitle(guide.getId(), guideTitle);
                contentItemRepository.save(guideContentItem);
            });
        } catch (Exception e) {
            log.error("Error enrichGuideWithContent", e);
            guideContentItems.forEach(it -> it.setStatus(ProcessingStatus.FAILED));
            contentItemRepository.saveAll(guideContentItems);
        }

        log.info("end enrichGuideWithContent for guide: {}", guide.getId());
    }


    private static String enrichWithContentImages(String guideDetailsWithoutImages,
                                                  Map<String, ImageDto> titleToImageMap) {
        var result = guideDetailsWithoutImages;

        for (var title : titleToImageMap.keySet()) {
            var target = String.format("{%s}", title);
            var titleStr = GptAnswerResolveHelper.replaceQuotes(title);
            var image = titleToImageMap.get(title);

            if (image != null && image.url() != null) {
                var replacement = String.format(MARKDOWN_IMAGE_FORMAT + "\n", image.url(), titleStr);
                result = result.replace(target, replacement);
            } else {
                log.warn("Not found image for title {}", title);
            }
        }

        return result;
    }

    public List<TravelGuideContentItem> createBlueprintContentItems(long guideId, String initialTitle, Long imageId) {
        var image = imageService.findByIdOrThrow(imageId);
        var imageStr = String.format(MARKDOWN_IMAGE_FORMAT, replaceQuotes(image.url()), initialTitle);
        var guideContentItem = TravelGuideContentItem.builder()
                // NOTE: initial content
                .content(String.format("# %s\n%s\n\n## Введение\n\n", initialTitle, imageStr))
                .guideId(guideId)
                .ordinal(1)
                .type(GuideContentItemType.MARKDOWN)
                .status(ProcessingStatus.IN_PROGRESS)
                .build();

        return List.of(contentItemRepository.save(guideContentItem));
    }
}
