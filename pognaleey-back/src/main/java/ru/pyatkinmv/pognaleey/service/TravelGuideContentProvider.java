package ru.pyatkinmv.pognaleey.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.pyatkinmv.pognaleey.dto.ImageDto;
import ru.pyatkinmv.pognaleey.model.TravelGuide;
import ru.pyatkinmv.pognaleey.model.TravelGuideContentItem;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public abstract class TravelGuideContentProvider {
    static final String MARKDOWN_IMAGE_FORMAT =
            "<img src=\"%s\" alt=\"%s\" style=\"width: 55rem; display: block; margin: 0 auto;\">";
    final ImageService imageService;

    static String enrichWithContentImages(String guideDetailsWithoutImages,
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

    abstract void enrichGuideWithContent(TravelGuide guide, List<TravelGuideContentItem> guideContentItems, long inquiryId,
                                         String recommendationTitle);

    public abstract List<TravelGuideContentItem> createBlueprintContentItems(long guideId,
                                                                             String initialTitle,
                                                                             Long imageId);

    List<ImageDto> searchAndSaveImages(List<GptAnswerResolveHelper.SearchableItem> searchableItems) {
        return searchableItems.stream()
                .map(it -> imageService.searchImageAndSave(it.title(), it.imageSearchPhrase()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}
