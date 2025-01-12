package ru.pyatkinmv.pognaleey.service;

import lombok.extern.slf4j.Slf4j;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.pyatkinmv.pognaleey.service.TravelRecommendationService.RECOMMENDATIONS_NUMBER;

@Slf4j
public class GptAnswerResolveHelper {
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

    public static String stripCurlyBraces(String input) {
        var regex = "\\{.*?\\}";
        var pattern = Pattern.compile(regex);
        var matcher = pattern.matcher(input);

        var result = input;

        while (matcher.find()) {
            var toRemove = matcher.group();
            log.info("Remove {}", toRemove);
            result = result.replace(toRemove, "");
        }

        return result;
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

    // TODO: doc
    static List<SearchableItem> parseSearchableItems(String searchableItemsRaw) {
        // {title1}(searchPhrase1)|{title2}(searchPhrase1)|...
        var regex = "([^|]+)";
        var pattern = Pattern.compile(regex);
        var matcher = pattern.matcher(searchableItemsRaw);
        var result = new ArrayList<SearchableItem>();

        while (matcher.find()) {
            var searchableItemRaw = matcher.group().trim();
            parseSearchableItem(searchableItemRaw)
                    .ifPresentOrElse(
                            result::add,
                            () -> log.warn("Wrong format for recommendation: {}", searchableItemRaw)
                    );
        }

        return result;
    }

    static Optional<SearchableItem> parseSearchableItem(String searchableItemRaw) {
        // {title}(searchPhrase)
        //noinspection RegExpRedundantEscape
        var regex = "\\{([^\\]]+)\\}\\(([^\\)]+)\\)";
        var pattern = Pattern.compile(regex);
        var matcher = pattern.matcher(searchableItemRaw);

        if (matcher.matches()) {
            var title = matcher.group(1);
            var imageSearchPhrase = matcher.group(2);

            return Optional.of(new SearchableItem(title, imageSearchPhrase));
        } else {
            return Optional.empty();
        }
    }

    public static String replaceQuotes(String text) {
        var result = new StringBuilder();
        var openQuote = true;

        for (char c : text.toCharArray()) {
            if (c == '\"') {
                result.append(openQuote ? "«" : "»");
                openQuote = !openQuote;
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    record SearchableItem(String title, String imageSearchPhrase) {

    }
}
