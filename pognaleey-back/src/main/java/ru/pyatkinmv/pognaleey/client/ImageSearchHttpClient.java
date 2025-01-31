package ru.pyatkinmv.pognaleey.client;

import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.pyatkinmv.pognaleey.dto.ImageSearchClientImageDto;

@RequiredArgsConstructor
@Slf4j
public abstract class ImageSearchHttpClient<T> {

  private static boolean hasMoreThanTwoWords(String searchQuery) {
    var queryWords = Arrays.stream(searchQuery.split(" ")).toList();

    return queryWords.size() > 2;
  }

  private static String shortenSearchQuery(String searchQuery) {
    var queryWords = Arrays.stream(searchQuery.split(" ")).toList();
    var newQueryWords = queryWords.subList(0, queryWords.size() - 1);

    return String.join(" ", newQueryWords);
  }

  Optional<T> makeRequest(URI uri) {
    T response = null;

    try {
      response = doMakeRequest(uri);
    } catch (Exception e) {
      log.error("could not get response", e);
    }

    return Optional.ofNullable(response);
  }

  abstract T doMakeRequest(URI uri);

  public Optional<ImageSearchClientImageDto> searchImage(String searchQuery) {
    log.info("searchImage for searchQuery {}", searchQuery);

    if (searchQuery.isEmpty()) {
      log.info("searchQuery is empty");

      return Optional.empty();
    }

    var uri = buildUri(searchQuery);
    log.info("searchImage uri {}", withoutSecret(uri));

    var response = makeRequest(uri);

    if (response.isEmpty()) {
      return Optional.empty();
    }

    var image = response.flatMap(it -> retrieveTargetImages(it, searchQuery));

    if (image.isEmpty()) {
      log.info("Couldn't find image for searchQuery {}", searchQuery);

      if (enableShorteningSearchQueryHack() && hasMoreThanTwoWords(searchQuery)) {
        log.info("Try to shorten searchQuery and search again");

        return searchImage(shortenSearchQuery(searchQuery));
      }
    }

    log.info("searchImage result {}", image);

    return image;
  }

  abstract URI buildUri(String searchQuery);

  protected boolean enableShorteningSearchQueryHack() {
    return false;
  }

  private String withoutSecret(URI uri) {
    return uri.toString().replace(getApiKey(), "SECRET");
  }

  abstract Optional<ImageSearchClientImageDto> retrieveTargetImages(T response, String searchQuery);

  abstract String getApiKey();
}
