package ru.pyatkinmv.pognaleey.controller;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.pyatkinmv.pognaleey.dto.TravelInquiryDto;
import ru.pyatkinmv.pognaleey.dto.TravelRecommendationListDto;
import ru.pyatkinmv.pognaleey.service.TravelInquiryService;

import java.util.Map;

@RestController
@RequestMapping("/travel-inquiries")
@RequiredArgsConstructor
@Slf4j
public class TravelInquiryController {
    private final TravelInquiryService inquiryService;

    @SneakyThrows
    @PostMapping
    public TravelInquiryDto createInquiry(@RequestBody Map<String, Object> inquiryParams) {
        return inquiryService.createInquiry(inquiryParams);
    }

    @SneakyThrows
    @GetMapping("/{inquiryId}/recommendations")
    public TravelRecommendationListDto getInquiryRecommendations(@PathVariable Long inquiryId) {
        return inquiryService.getInquiryRecommendations(inquiryId, 30_000L);
    }
}
