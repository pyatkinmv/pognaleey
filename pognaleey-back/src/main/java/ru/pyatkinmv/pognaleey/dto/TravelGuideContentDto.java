package ru.pyatkinmv.pognaleey.dto;

import java.util.List;
import org.springframework.lang.Nullable;

public record TravelGuideContentDto(List<TravelGuideContentItemDto> contentItems) {
  public record TravelGuideContentItemDto(
      long id, long guideId, int ordinal, @Nullable String content, String status, String type) {}
}
