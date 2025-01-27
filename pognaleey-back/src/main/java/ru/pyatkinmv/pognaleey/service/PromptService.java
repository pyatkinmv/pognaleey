package ru.pyatkinmv.pognaleey.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import ru.pyatkinmv.pognaleey.dto.GptResponseRecommendationDetailsDto;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;
import ru.pyatkinmv.pognaleey.util.Utils;

@Service
@RequiredArgsConstructor
public class PromptService {
    // TODO: разобраться с этими каунтами
    public static final int GUIDE_PRACTICAL_TITLES_COUNT = 5;

    private final MessageSource messageSource;

    static final GptResponseRecommendationDetailsDto DETAILED_PROMPT_OBJ = new GptResponseRecommendationDetailsDto(
            "ПОЧЕМУ ЭТОТ ВАРИАНТ ПОДХОДИТ",
            "ОПИСАНИЕ ВАРИАНТА"
    );

    public String generateGuideIntroPrompt(String guideTitle, String inquiryParams) {
        return messageSource.getMessage(
                "prompts.guide_intro",
                new Object[]{guideTitle, inquiryParams},
                LanguageContextHolder.getLanguageLocale()
        );
    }

    public String generateGuidePracticalTitlesPrompt(String guideTitle, String inquiryParams) {
        return messageSource.getMessage(
                "prompts.guide_practical_topics",
                new Object[]{guideTitle, inquiryParams, GUIDE_PRACTICAL_TITLES_COUNT},
                LanguageContextHolder.getLanguageLocale()
        );
    }

    public String generateGuidePracticalTitlePrompt(String guideTitle, String inquiryParams, String allTitles, String practicalTitle) {
        return messageSource.getMessage(
                "prompts.guide_generate_for_one_topic_from_list",
                new Object[]{guideTitle, inquiryParams, allTitles, practicalTitle},
                LanguageContextHolder.getLanguageLocale()
        );
    }

    public String generateGuideAttractionsPrompt(String guideTitle, String inquiryParams, String guideVisualTopics) {
        return messageSource.getMessage(
                "prompts.guide_generate_attractions_part",
                new Object[]{guideTitle, inquiryParams, GUIDE_PRACTICAL_TITLES_COUNT, guideVisualTopics},
                LanguageContextHolder.getLanguageLocale()
        );
    }

    public String generateGuideConclusionPrompt(String guideTitle, String inquiryParams) {
        return messageSource.getMessage(
                "prompts.guide_conclusion",
                new Object[]{guideTitle, inquiryParams},
                LanguageContextHolder.getLanguageLocale()
        );
    }

    public String generateQuickPrompt(int optionsNumber, String inquiryParams) {
        return messageSource.getMessage(
                "prompts.generate_ideas",
                new Object[]{optionsNumber, inquiryParams},
                LanguageContextHolder.getLanguageLocale()
        );
    }

    public String generateDetailedPrompt(TravelRecommendation recommendation, String inquiryParams) {
        var recommendationsStr = toPromtString(recommendation);

        return messageSource.getMessage(
                "prompts.recommendations_details",
                new Object[]{recommendationsStr, inquiryParams, Utils.toJson(DETAILED_PROMPT_OBJ)},
                LanguageContextHolder.getLanguageLocale()
        );
    }

    private static String toPromtString(TravelRecommendation recommendation) {
        return recommendation.getTitle();
    }

    public String generateGuideImagesPrompt(String guideTitle, String inquiryParams) {
        return messageSource.getMessage(
                "prompts.generate_guide_images",
                new Object[]{guideTitle, inquiryParams, GUIDE_PRACTICAL_TITLES_COUNT},
                LanguageContextHolder.getLanguageLocale()
        );
    }

    public String generateCreateGuidePrompt(String title, String inquiryParams, String guideTopics) {
        return messageSource.getMessage(
                "prompts.generate_full_guide_deprecated",
                new Object[]{title, inquiryParams, GUIDE_PRACTICAL_TITLES_COUNT, guideTopics, GUIDE_PRACTICAL_TITLES_COUNT},
                LanguageContextHolder.getLanguageLocale()
        );
    }
}
