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
  static final int GUIDE_PRACTICAL_TITLES_COUNT = 5;
  static final int GUIDE_ATTRACTIONS_COUNT = 5;

  private final MessageSource messageSource;

  private static String toPromtString(TravelRecommendation recommendation) {
    return recommendation.getTitle();
  }

  String buildExampleResponseRecommendationDetailsDto() {
    return Utils.toJson(
        new GptResponseRecommendationDetailsDto(
            messageSource.getMessage(
                "prompts.detailed_recommendations_example.reason",
                null,
                LanguageContextHolder.getLanguageLocaleOrDefault()),
            messageSource.getMessage(
                "prompts.detailed_recommendations_example.description",
                null,
                LanguageContextHolder.getLanguageLocaleOrDefault())));
  }

  public String generateGuideIntroPrompt(String guideTitle, String inquiryParams) {
    return messageSource.getMessage(
        "prompts.guide_intro",
        new Object[] {guideTitle, inquiryParams},
        LanguageContextHolder.getLanguageLocaleOrDefault());
  }

  public String generateGuidePracticalTitlesPrompt(String guideTitle, String inquiryParams) {
    return messageSource.getMessage(
        "prompts.guide_practical_topics",
        new Object[] {guideTitle, inquiryParams, GUIDE_PRACTICAL_TITLES_COUNT},
        LanguageContextHolder.getLanguageLocaleOrDefault());
  }

  public String generateGuidePracticalTitlePrompt(
      String guideTitle, String inquiryParams, String allTitles, String practicalTitle) {
    return messageSource.getMessage(
        "prompts.guide_generate_for_one_topic_from_list",
        new Object[] {guideTitle, inquiryParams, allTitles, practicalTitle},
        LanguageContextHolder.getLanguageLocaleOrDefault());
  }

  public String generateGuideAttractionsPrompt(
      String guideTitle, String inquiryParams, String guideVisualTopics) {
    return messageSource.getMessage(
        "prompts.guide_generate_attractions_part",
        new Object[] {guideTitle, inquiryParams, GUIDE_ATTRACTIONS_COUNT, guideVisualTopics},
        LanguageContextHolder.getLanguageLocaleOrDefault());
  }

  public String generateGuideConclusionPrompt(String guideTitle, String inquiryParams) {
    return messageSource.getMessage(
        "prompts.guide_conclusion",
        new Object[] {guideTitle, inquiryParams},
        LanguageContextHolder.getLanguageLocaleOrDefault());
  }

  public String generateQuickPrompt(int optionsNumber, String inquiryParams) {
    return messageSource.getMessage(
        "prompts.generate_ideas",
        new Object[] {optionsNumber, inquiryParams},
        LanguageContextHolder.getLanguageLocaleOrDefault());
  }

  public String generateDetailedPrompt(TravelRecommendation recommendation, String inquiryParams) {
    var recommendationsStr = toPromtString(recommendation);

    return messageSource.getMessage(
        "prompts.recommendations_details",
        new Object[] {
          recommendationsStr, inquiryParams, buildExampleResponseRecommendationDetailsDto()
        },
        LanguageContextHolder.getLanguageLocaleOrDefault());
  }

  public String generateGuideImagesPrompt(String guideTitle, String inquiryParams) {
    return messageSource.getMessage(
        "prompts.generate_guide_images",
        new Object[] {guideTitle, inquiryParams, GUIDE_ATTRACTIONS_COUNT},
        LanguageContextHolder.getLanguageLocaleOrDefault());
  }
}
