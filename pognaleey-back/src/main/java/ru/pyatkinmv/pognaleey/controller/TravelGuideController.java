package ru.pyatkinmv.pognaleey.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.pyatkinmv.pognaleey.dto.TravelGuideContentDto;
import ru.pyatkinmv.pognaleey.dto.TravelGuideInfoDto;
import ru.pyatkinmv.pognaleey.dto.TravelGuideLikeDto;
import ru.pyatkinmv.pognaleey.service.TravelGuideService;

@RestController
@RequestMapping("/travel-guides")
@RequiredArgsConstructor
@Slf4j
public class TravelGuideController {
  private final TravelGuideService travelGuideService;

  @PostMapping
  public TravelGuideInfoDto createGuide(@RequestParam("recommendationId") long recommendationId) {
    return travelGuideService.createGuide(recommendationId);
  }

  @PutMapping("/{guideId}/like")
  public TravelGuideLikeDto likeGuide(@PathVariable("guideId") long guideId) {
    return travelGuideService.likeGuide(guideId);
  }

  @DeleteMapping("/{guideId}/unlike")
  public TravelGuideLikeDto unlikeGuide(@PathVariable("guideId") long guideId) {
    return travelGuideService.unlikeGuide(guideId);
  }

  @GetMapping("/{guideId}")
  public TravelGuideInfoDto getGuide(@PathVariable("guideId") long guideId) {
    return travelGuideService.getGuideInfo(guideId);
  }

  @GetMapping("/{guideId}/content")
  public TravelGuideContentDto getGuideContent(@PathVariable("guideId") long guideId) {
    return travelGuideService.getGuideContent(guideId);
  }

  @GetMapping("/liked")
  public Page<TravelGuideInfoDto> getLikedGuides(Pageable pageable) {
    return travelGuideService.getLikedGuides(pageable);
  }

  @GetMapping("/my")
  public Page<TravelGuideInfoDto> getMyGuides(Pageable pageable) {
    return travelGuideService.getMyGuides(pageable);
  }

  @GetMapping("/feed")
  public Page<TravelGuideInfoDto> getFeedGuides(Pageable pageable) {
    return travelGuideService.getFeedGuides(pageable);
  }
}
