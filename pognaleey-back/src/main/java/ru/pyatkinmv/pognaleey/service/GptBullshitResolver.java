package ru.pyatkinmv.pognaleey.service;

import lombok.extern.slf4j.Slf4j;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;

import java.util.List;

import static ru.pyatkinmv.pognaleey.service.TravelRecommendationService.RECOMMENDATIONS_NUMBER;

@Slf4j
public class GptBullshitResolver {
    public static List<TravelRecommendation> resolveInCaseGeneratedMoreOrLessThanExpected(
            List<TravelRecommendation> recommendations) {
        if (recommendations.size() != RECOMMENDATIONS_NUMBER) {
            log.warn("Number of recommendations {} is not equal to the number of expected {}",
                    recommendations.size(), RECOMMENDATIONS_NUMBER);

            if (recommendations.size() < RECOMMENDATIONS_NUMBER) {
                return recommendations;
            } else {
                return recommendations.subList(0, RECOMMENDATIONS_NUMBER);
            }
        }

        return recommendations;
    }
}
