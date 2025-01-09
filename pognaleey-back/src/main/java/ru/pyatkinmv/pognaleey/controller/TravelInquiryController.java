package ru.pyatkinmv.pognaleey.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.pyatkinmv.pognaleey.dto.TravelInquiryDto;
import ru.pyatkinmv.pognaleey.dto.TravelRecommendationListDto;
import ru.pyatkinmv.pognaleey.service.TravelInquiryService;
import ru.pyatkinmv.pognaleey.util.Utils;

import java.util.Map;

@RestController
@RequestMapping("/travel-inquiries")
@RequiredArgsConstructor
@Slf4j
public class TravelInquiryController {
    private final TravelInquiryService inquiryService;

    @PostMapping
    public TravelInquiryDto createInquiry(@RequestBody Map<String, Object> inquiryParams) {
        return Utils.measuringTime(() -> inquiryService.createInquiry(inquiryParams));
    }

    @GetMapping("/{inquiryId}/recommendations")
    public TravelRecommendationListDto getInquiryRecommendations(@PathVariable Long inquiryId) {
        return Utils.measuringTime(() -> inquiryService.getInquiryRecommendations(inquiryId, 30_000L));
    }
}
