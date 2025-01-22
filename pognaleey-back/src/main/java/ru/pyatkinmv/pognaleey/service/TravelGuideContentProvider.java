package ru.pyatkinmv.pognaleey.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.pyatkinmv.pognaleey.client.ImagesSearchHttpClient;
import ru.pyatkinmv.pognaleey.model.TravelGuide;
import ru.pyatkinmv.pognaleey.model.TravelGuideContentItem;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public abstract class TravelGuideContentProvider {
    private final ImagesSearchHttpClient imagesSearchHttpClient;

    static final String MARKDOWN_IMAGE_FORMAT = "<img src=\"%s\" alt=\"%s\" style=\"width: 45rem; display: block; margin: 0 auto;\">";

    static String enrichWithContentImages(String guideDetailsWithoutImages, Map<String, String> titleToImageUrlMap) {
        var result = guideDetailsWithoutImages;

        for (var title : titleToImageUrlMap.keySet()) {
            var target = String.format("{%s}", title);
            var titleStr = GptAnswerResolveHelper.replaceQuotes(title);
            String imageUrl = titleToImageUrlMap.get(title);

            if (imageUrl != null) {
                var replacement = String.format(MARKDOWN_IMAGE_FORMAT + "\n", imageUrl, titleStr);
                result = result.replace(target, replacement);
            } else {
                log.warn("Not found image for title {}", title);
            }
        }

        return result;
    }

    abstract void enrichGuideWithContent(TravelGuide guide, List<TravelGuideContentItem> guideContentItems, long inquiryId,
                                         String recommendationTitle);

    public abstract List<TravelGuideContentItem> createBlueprintContentItems(long guideId, String initialTitle, String imageUrl);

    Map<String, String> searchImagesWithSleepAndBuildTitleToImageMap(List<GptAnswerResolveHelper.SearchableItem> titlesWithImageSearchPhrases) {
        return titlesWithImageSearchPhrases.stream()
                .map(it -> Map.entry(
                        it.title(),
                        imagesSearchHttpClient.searchImageUrlWithRateLimiting(it.imageSearchPhrase()))
                ).filter(it -> it.getValue().isPresent())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        it -> it.getValue().get(),
                        (a, b) -> b,
                        () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER)
                ));
    }
}
