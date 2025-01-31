package ru.pyatkinmv.pognaleey.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.pyatkinmv.pognaleey.dto.TravelInquiryDto;
import ru.pyatkinmv.pognaleey.service.TravelInquiryService;
import ru.pyatkinmv.pognaleey.util.Utils;

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
}
