package ru.pyatkinmv.pognaleey.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.pyatkinmv.pognaleey.dto.ImageDto;
import ru.pyatkinmv.pognaleey.model.TravelGuide;
import ru.pyatkinmv.pognaleey.model.TravelGuideContentItem;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public abstract class TravelGuideContentProvider {
    final ImageService imageService;

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
