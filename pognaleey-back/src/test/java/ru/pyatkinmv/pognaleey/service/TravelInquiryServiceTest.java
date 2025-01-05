package ru.pyatkinmv.pognaleey.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.pyatkinmv.pognaleey.DatabaseCleaningTest;
import ru.pyatkinmv.pognaleey.config.ClientsConfig;
import ru.pyatkinmv.pognaleey.dto.TravelInquiryDto;
import ru.pyatkinmv.pognaleey.dto.TravelQuickRecommendationDto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Import(ClientsConfig.class)
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
                        new TravelQuickRecommendationDto(-1L, "Токио", "современный мегаполис"),
                        new TravelQuickRecommendationDto(-1L, "Сингапур", "город-государство контрастов"),
                        new TravelQuickRecommendationDto(-1L, "Бангкок", "тайский колорит и экзотика")
                );

    }

    @Test
    void getInquiryRecommendations() {
        var inquiry = travelInquiryService.createInquiry(Map.of("preferences", "festivals", "to", "europe"));
        var inquiryRecommendations = travelInquiryService.getInquiryRecommendations(inquiry.getId(), 100L);
        assertThat(inquiryRecommendations.recommendations()).hasSize(3);
        assertThat(inquiryRecommendations.recommendations()).allMatch(
                it -> it.title() != null
                        && it.imageUrl().equals("imageUrl")
                        && it.budget() != null
                        && it.additionalConsideration() != null
                        && it.creativeDescription() != null
                        && it.reasoning() != null
                        && it.tips() != null
                        && !it.whereToGo().isEmpty()
        );
    }
}