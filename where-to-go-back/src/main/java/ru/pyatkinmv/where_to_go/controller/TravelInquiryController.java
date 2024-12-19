package ru.pyatkinmv.where_to_go.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import ru.pyatkinmv.where_to_go.service.TravelInquiryService;
import ru.pyatkinmv.where_to_go.util.Utils;

import java.time.Instant;
import java.util.Map;

import static ru.pyatkinmv.where_to_go.util.Utils.measuringTime;

@RestController
@RequestMapping("/travel-inquiries")
@RequiredArgsConstructor
public class TravelInquiryController {
    private final TravelInquiryService inquiryService;
    private final TemplateEngine templateEngine;

    @PostMapping
    public String createInquiry(@RequestParam Map<String, String> inquiryParams) {
        return measuringTime(() -> {
            var inquiry = inquiryService.createInquiry(inquiryParams);
            Context context = new Context();
            context.setVariable("options", inquiry.getQuickOptions());
            context.setVariable("inquiryId", inquiry.getId());

            return templateEngine.process("quick-recommendation-template", context);
        });
    }

    @GetMapping("/{inquiryId}/recommendations")
    public String getDetailedRecommendation(@PathVariable Long inquiryId) {
        return measuringTime(() -> {
            var inquiry = inquiryService.getInquiryWithDetailedRecommendation(inquiryId, 15_000L);
            Context context = new Context();
            context.setVariable("options", inquiry.getDetailedOptions());

            return templateEngine.process("detailed-recommendation-template", context);
        });
    }
}
