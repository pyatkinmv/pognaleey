package ru.pyatkinmv.where_to_go.controller;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import ru.pyatkinmv.where_to_go.dto.TravelInquiryDto;
import ru.pyatkinmv.where_to_go.dto.TravelRecommendationDetailedOptionListDto;
import ru.pyatkinmv.where_to_go.service.TravelInquiryService;

import java.util.Map;

import static ru.pyatkinmv.where_to_go.mapper.TravelInquiryMapper.OBJECT_MAPPER;

// TODO: fixme
@CrossOrigin
@RestController
@RequestMapping("/travel-inquiries")
@RequiredArgsConstructor
@Slf4j
public class TravelInquiryController {
    private final TravelInquiryService inquiryService;
    private final TemplateEngine templateEngine;

    @SneakyThrows
    @PostMapping
    public TravelInquiryDto createInquiry(@RequestBody String inquiryParams) {
        TravelInquiryDto inquiry = inquiryService.createInquiry(OBJECT_MAPPER.readValue(inquiryParams, Map.class));
        log.info("RESPONSE: {}", OBJECT_MAPPER.writeValueAsString(inquiry));

        return inquiry;
//        return measuringTime(() -> {
//            var inquiry = inquiryService.createInquiry(inquiryParams);
//            Context context = new Context();
//            context.setVariable("recommendations", inquiry.getQuickOptions());
//            context.setVariable("inquiryId", inquiry.getId());
//
//            return templateEngine.process("quick-recommendation-template", context);
//        });
    }

    @SneakyThrows
    @GetMapping("/{inquiryId}/recommendations")
    public TravelRecommendationDetailedOptionListDto getDetailedRecommendation(@PathVariable Long inquiryId) {
        TravelRecommendationDetailedOptionListDto travelRecommendationDetailedOptionListDto = new TravelRecommendationDetailedOptionListDto(
                inquiryService.getInquiryWithDetailedRecommendation(inquiryId, 15_000L)
                        .getDetailedRecommendations()
        );

        log.info("RESPONSE: {}", OBJECT_MAPPER.writeValueAsString(travelRecommendationDetailedOptionListDto));
        return travelRecommendationDetailedOptionListDto;
//        return measuringTime(() -> {
//            var inquiry = inquiryService.getInquiryWithDetailedRecommendation(inquiryId, 15_000L);
//            Context context = new Context();
//            context.setVariable("recommendations", inquiry.getDetailedOptions());
//
//            return templateEngine.process("detailed-recommendation-template", context);
//        });
    }
}
