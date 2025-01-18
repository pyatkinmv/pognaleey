package ru.pyatkinmv.pognaleey.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.pyatkinmv.pognaleey.DatabaseCleaningTest;
import ru.pyatkinmv.pognaleey.dto.TravelInquiryDto;
import ru.pyatkinmv.pognaleey.dto.TravelQuickRecommendationDto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class TravelInquiryServiceTest extends DatabaseCleaningTest {
    @Autowired
    private TravelInquiryService travelInquiryService;

    @Test
    void createInquiry() {
        var inquiry = travelInquiryService.createInquiry(Map.of("budget", "1000", "purpose", "relax"));
        assertThat(inquiry).usingRecursiveComparison().ignoringFields("id", "createdAt", "quickRecommendations")
                .isEqualTo(new TravelInquiryDto(-1L, "purpose=relax;budget=1000", Instant.now(), List.of()));

        assertThat(inquiry.getQuickRecommendations()).hasSize(3);
        assertThat(inquiry.getQuickRecommendations())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .containsExactly(
                        new TravelQuickRecommendationDto(-1L, "Грузия, Тбилиси и винные регионы"),
                        new TravelQuickRecommendationDto(-1L, "Париж, Франция: Город любви"),
                        new TravelQuickRecommendationDto(-1L, "Красная Поляна, Сочи: Горнолыжный отдых")
                );

    }

    // TODO: FIX TESTS
    @Test
    void getInquiryRecommendations() {
        var inquiry = travelInquiryService.createInquiry(Map.of("preferences", "festivals", "to", "europe"));
        var inquiryRecommendations = travelInquiryService.getInquiryRecommendations(inquiry.getId(), 100L);
        assertThat(inquiryRecommendations.recommendations()).hasSize(3);
        assertThat(inquiryRecommendations.recommendations()).allMatch(
                it -> it.title() != null
                        && it.imageUrl().equals("imageUrl")
                        && it.description() != null
                        && it.reasoning() != null
        );
    }
}