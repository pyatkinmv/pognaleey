package ru.pyatkinmv.pognaleey.service;

import org.junit.jupiter.api.Test;
import ru.pyatkinmv.pognaleey.dto.ImageDto;
import ru.pyatkinmv.pognaleey.model.TravelGuideContentItem;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.pyatkinmv.pognaleey.model.GuideContentItemType.IMAGE;
import static ru.pyatkinmv.pognaleey.model.GuideContentItemType.MARKDOWN;
import static ru.pyatkinmv.pognaleey.model.ProcessingStatus.IN_PROGRESS;
import static ru.pyatkinmv.pognaleey.service.TravelGuideContentProviderV2.GuideStructureType.SIGHTSEEING;

class TravelGuideContentProviderV2Test {

    @Test
    void extractItems() {
        var sightseeingContentResponse = """
                ## Main header
                ### topic1
                text1
                {topic1}
                ### topic2
                text2
                {topic2-corrupted}
                ### topic3
                text3
                {topic3}
                ### topic4
                text4
                {topic4}
                """;
        var titleToImageMap = Map.of(
                "topic1", new ImageDto("url1", "title1", "thumb1", "q1"),
                "topic2", new ImageDto("url2", "title2", "thumb2", "q2"),
                "topic3", new ImageDto("url3", "title3", "thumb3", "q3")
        );
        var guideId = 157L;
        int firstOrdinal = SIGHTSEEING.ordinal();
        var sightseeingFirst = new TravelGuideContentItem(null, guideId, null, firstOrdinal, IN_PROGRESS, MARKDOWN);
        var contentItems = TravelGuideContentProviderV2.extractItems(sightseeingContentResponse, sightseeingFirst, titleToImageMap);

        assertThat(contentItems)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .containsExactly(
                        new TravelGuideContentItem(null, guideId, "## Main header\n", 3, IN_PROGRESS, MARKDOWN),
                        new TravelGuideContentItem(null, guideId, "### topic1\ntext1\n\n", 4, IN_PROGRESS, MARKDOWN),
                        new TravelGuideContentItem(null, guideId, "{\"id\":null,\"title\":\"url1\",\"url\":\"title1\",\"thumbnailUrl\":\"thumb1\",\"query\":\"q1\",\"licenceUrl\":null,\"authorName\":null,\"authorUrl\":null}", 5, IN_PROGRESS, IMAGE),
                        new TravelGuideContentItem(null, guideId, "### topic2\ntext2\n\n", 6, IN_PROGRESS, MARKDOWN),
                        new TravelGuideContentItem(null, guideId, "### topic3\ntext3\n\n", 7, IN_PROGRESS, MARKDOWN),
                        new TravelGuideContentItem(null, guideId, "{\"id\":null,\"title\":\"url3\",\"url\":\"title3\",\"thumbnailUrl\":\"thumb3\",\"query\":\"q3\",\"licenceUrl\":null,\"authorName\":null,\"authorUrl\":null}", 8, IN_PROGRESS, IMAGE),
                        new TravelGuideContentItem(null, guideId, "### topic4\ntext4\n\n", 9, IN_PROGRESS, MARKDOWN)
                );

    }

}