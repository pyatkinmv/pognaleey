package ru.pyatkinmv.pognaleey.dto;

import java.util.List;
import org.springframework.lang.Nullable;

public record OpenverseImagesResponseDto(List<Photo> results) {
  public record Photo(
      String id,
      String creator,
      String creator_url,
      String title,
      String license_url,
      String url,
      String thumbnail,
      @Nullable Integer height,
      @Nullable Integer width) {}
}
