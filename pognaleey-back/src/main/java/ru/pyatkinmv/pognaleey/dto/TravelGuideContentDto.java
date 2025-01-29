package ru.pyatkinmv.pognaleey.dto;

import org.springframework.lang.Nullable;

import java.util.List;

public record TravelGuideContentDto(List<TravelGuideContentItemDto> contentItems) {
    public record TravelGuideContentItemDto(long id, long guideId, int ordinal, @Nullable String content, String status,
                                            String type) {

    }
}
