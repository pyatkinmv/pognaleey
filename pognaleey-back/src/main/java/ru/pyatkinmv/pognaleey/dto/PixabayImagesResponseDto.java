package ru.pyatkinmv.pognaleey.dto;

import java.util.List;

public record PixabayImagesResponseDto(List<ImageHit> hits) {

  public record ImageHit(Long id, String webformatURL, String largeImageURL) {}
}
