package ru.pyatkinmv.pognaleey.service;

import static ru.pyatkinmv.pognaleey.service.TravelRecommendationService.RECOMMENDATIONS_IDEAS_NUMBER;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import ru.pyatkinmv.pognaleey.dto.SearchableItemDto;

@Slf4j
public class GptAnswerResolveHelper {
  public static <T> List<T> resolveInCaseGeneratedMoreOrLessThanExpected(List<T> recommendations) {
    if (recommendations.size() != RECOMMENDATIONS_IDEAS_NUMBER) {
      log.warn(
          "Number of recommendations {} is not equal to the number of expected {}",
          recommendations.size(),
          RECOMMENDATIONS_IDEAS_NUMBER);

      if (recommendations.size() < RECOMMENDATIONS_IDEAS_NUMBER) {
        return recommendations;
      } else {
        return recommendations.subList(0, RECOMMENDATIONS_IDEAS_NUMBER);
      }
    }

    return recommendations;
  }

  public static String removeJsonTagsIfPresent(String json) {
    return json.trim().replace("\\(json\\)", "").replace("json", "");
  }

  public static Optional<String> findFirstByRegex(String text, String regex) {
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(text);

    if (matcher.find()) {
      return Optional.of(matcher.group());
    } else {
      return Optional.empty();
    }
  }

  /**
   * Parses a string containing searchable items and returns a list of {@link SearchableItemDto}
   * objects.
   *
   * <p>The input string should be in the format
   * "{title1}(searchPhrase1)|{title2}(searchPhrase2)|...". This method uses a regular expression to
   * extract individual items and then parses each item using the method {@link
   * #parseSearchableItem(String)}.
   *
   * @param searchableItemsRaw the raw string containing searchable items, formatted as
   *     "{title1}(searchPhrase1)|{title2}(searchPhrase2)|..."
   * @return a list of parsed {@link SearchableItemDto} objects
   * @see #parseSearchableItem(String)
   */
  static List<SearchableItemDto> parseSearchableItems(String searchableItemsRaw) {
    var regex = "([^|]+)";
    var pattern = Pattern.compile(regex);
    var matcher = pattern.matcher(searchableItemsRaw);
    var result = new ArrayList<SearchableItemDto>();

    while (matcher.find()) {
      var searchableItemRaw = matcher.group().trim().replaceAll("\\.", "");
      parseSearchableItem(searchableItemRaw)
          .ifPresentOrElse(
              result::add,
              () -> log.warn("Wrong format for recommendation: {}", searchableItemRaw));
    }

    return result;
  }

  static List<String> splitWithPipe(String input) {
    var regex = "([^|]+)";
    var pattern = Pattern.compile(regex);
    var matcher = pattern.matcher(input);
    var result = new ArrayList<String>();

    while (matcher.find()) {
      var item = matcher.group().trim().replaceAll("\\.", "");

      if (!item.isEmpty()) {
        result.add(item);
      }
    }

    return result;
  }

  static Optional<SearchableItemDto> parseSearchableItem(String searchableItemRaw) {
    //noinspection RegExpRedundantEscape
    var regex = "\\{([^\\]]+)\\}\\(([^\\)]+)\\)";
    var pattern = Pattern.compile(regex);
    var matcher = pattern.matcher(searchableItemRaw);

    if (matcher.matches()) {
      var title = matcher.group(1);
      var imageSearchPhrase = matcher.group(2);

      return Optional.of(new SearchableItemDto(title, imageSearchPhrase));
    } else {
      return Optional.empty();
    }
  }
}
