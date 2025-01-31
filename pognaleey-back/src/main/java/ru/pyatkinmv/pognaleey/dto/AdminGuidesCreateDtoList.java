package ru.pyatkinmv.pognaleey.dto;

import java.util.List;

public record AdminGuidesCreateDtoList(List<ManualGuideCreateDto> guides) {

  public record ManualGuideCreateDto(
      String inquiryParams, String recommendationTitle, String imageQuery) {}
}
