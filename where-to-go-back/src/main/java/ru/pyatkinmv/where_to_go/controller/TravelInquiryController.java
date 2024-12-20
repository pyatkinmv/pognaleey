package ru.pyatkinmv.where_to_go.controller;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
public class TravelInquiryController {
    private final TravelInquiryService inquiryService;
    private final TemplateEngine templateEngine;

    @SneakyThrows
    @PostMapping
    public TravelInquiryDto createInquiry(@RequestBody String inquiryParams) {
        return inquiryService.createInquiry(OBJECT_MAPPER.readValue(inquiryParams, Map.class));
//        return measuringTime(() -> {
//            var inquiry = inquiryService.createInquiry(inquiryParams);
//            Context context = new Context();
//            context.setVariable("recommendations", inquiry.getQuickOptions());
//            context.setVariable("inquiryId", inquiry.getId());
//
//            return templateEngine.process("quick-recommendation-template", context);
//        });
    }

    @GetMapping("/{inquiryId}/recommendations")
    public TravelRecommendationDetailedOptionListDto getDetailedRecommendation(@PathVariable Long inquiryId) {
        return new TravelRecommendationDetailedOptionListDto(
                inquiryService.getInquiryWithDetailedRecommendation(inquiryId, 15_000L)
                        .getDetailedOptions()
        );
//        return measuringTime(() -> {
//            var inquiry = inquiryService.getInquiryWithDetailedRecommendation(inquiryId, 15_000L);
//            Context context = new Context();
//            context.setVariable("recommendations", inquiry.getDetailedOptions());
//
//            return templateEngine.process("detailed-recommendation-template", context);
//        });
    }
}
