package ru.pyatkinmv.pognaleey.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.pyatkinmv.pognaleey.DatabaseCleaningTest;
import ru.pyatkinmv.pognaleey.dto.TravelInquiryDto;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class TravelInquiryServiceTest extends DatabaseCleaningTest {
    @Autowired
    private TravelInquiryService travelInquiryService;
    @Autowired
    private TravelRecommendationService travelRecommendationService;

    @Test
    void createInquiry() {
        var inquiry = travelInquiryService.createInquiry(Map.of("budget", "1000", "purpose", "relax"));
        assertThat(inquiry).usingRecursiveComparison().ignoringFields("id", "createdAt", "quickRecommendations")
                .isEqualTo(new TravelInquiryDto(-1L, "purpose=relax;budget=1000", Instant.now()));
        var recommendations = travelRecommendationService.getRecommendations(inquiry.getId());
        assertThat(recommendations.recommendations()).allMatch(
                it -> it.id() != 0
                        && it.title() != null
                        && it.details() != null
                        && it.image() != null
                        && it.guideId() == null
                        && it.status().equals("READY")
        );
    }
}