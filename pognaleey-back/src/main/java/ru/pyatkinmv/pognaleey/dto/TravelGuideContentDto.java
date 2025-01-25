package ru.pyatkinmv.pognaleey.dto;

import java.util.List;

public record TravelGuideContentDto(List<TravelGuideContentItemDto> contentItems) {
    public record TravelGuideContentItemDto(long id, long guideId, int ordinal, String content, String status,
                                            String type) {

    }
}
