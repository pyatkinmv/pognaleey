package ru.pyatkinmv.pognaleey.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.pyatkinmv.pognaleey.dto.TravelGuideFullDto;
import ru.pyatkinmv.pognaleey.dto.TravelGuideLikeDto;
import ru.pyatkinmv.pognaleey.dto.TravelGuideShortListDto;
import ru.pyatkinmv.pognaleey.model.User;
import ru.pyatkinmv.pognaleey.service.TravelGuideService;

@RestController
@RequestMapping("/travel-guides")
@RequiredArgsConstructor
@Slf4j
public class TravelGuideController {
    private final TravelGuideService travelGuideService;

    // TODO: Redesign
    @PostMapping
    public TravelGuideFullDto createGuide(@RequestParam("recommendationId") long recommendationId,
                                          @AuthenticationPrincipal @Nullable User user) {
        return travelGuideService.createGuide(recommendationId, user);
    }

    @PostMapping("/{guideId}/like")
    public TravelGuideLikeDto likeGuide(@PathVariable("guideId") long guideId,
                                        @AuthenticationPrincipal @Nullable User user) {
        return travelGuideService.likeGuide(guideId, user);
    }

    @DeleteMapping("/{guideId}/unlike")
    public TravelGuideLikeDto unlikeGuide(@PathVariable("guideId") long guideId,
                                          @AuthenticationPrincipal @Nullable User user) {
        return travelGuideService.unlikeGuide(guideId, user);
    }

    @GetMapping("/{guideId}")
    public TravelGuideFullDto getGuide(@PathVariable("guideId") long guideId,
                                       @AuthenticationPrincipal @Nullable User user) {
        return travelGuideService.getFullGuide(guideId, user);
    }

    @GetMapping("/liked")
    public TravelGuideShortListDto getLikedGuides(@AuthenticationPrincipal @Nullable User user) {
        return travelGuideService.getLikedGuides(user);
    }

    @GetMapping("/my")
    public TravelGuideShortListDto getMyGuides(@AuthenticationPrincipal @Nullable User user) {
        return travelGuideService.getMyGuides(user);
    }

    // TODO: Pagination?
    @GetMapping("/feed")
    public TravelGuideShortListDto getFeedGuides(@AuthenticationPrincipal @Nullable User user) {
        return travelGuideService.getFeedGuides(user);
    }

}
