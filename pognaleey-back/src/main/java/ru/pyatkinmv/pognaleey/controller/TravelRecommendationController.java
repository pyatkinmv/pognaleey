package ru.pyatkinmv.pognaleey.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.pyatkinmv.pognaleey.dto.TravelRecommendationListDto;
import ru.pyatkinmv.pognaleey.service.TravelRecommendationService;

@RestController
@RequestMapping("/travel-recommendations")
@RequiredArgsConstructor
@Slf4j
public class TravelRecommendationController {
  private final TravelRecommendationService recommendationService;

  @GetMapping
  public TravelRecommendationListDto getRecommendations(
      @RequestParam(required = false) @Nullable Long inquiryId,
      @RequestParam(required = false) @Nullable List<Long> ids) {
    if (inquiryId == null && CollectionUtils.isEmpty(ids)) {
      throw new IllegalArgumentException("inquiryId and ids can't be empty");
    }

    if (!CollectionUtils.isEmpty(ids)) {
      return recommendationService.getRecommendations(ids);
    }

    return recommendationService.getRecommendations(inquiryId);
  }
}
