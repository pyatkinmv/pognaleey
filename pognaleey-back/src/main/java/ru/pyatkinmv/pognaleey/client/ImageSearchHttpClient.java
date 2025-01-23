package ru.pyatkinmv.pognaleey.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.pyatkinmv.pognaleey.dto.ImageSearchClientImageDto;

import java.net.URI;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public abstract class ImageSearchHttpClient<T> {

    public Optional<ImageSearchClientImageDto> searchImage(String searchQuery) {
        log.info("searchImage for searchQuery {}", searchQuery);

        if (searchQuery.isEmpty()) {
            log.info("searchQuery is empty");

            return Optional.empty();
        }

        var uri = buildUri(searchQuery);
        log.info("searchImage uri {}", withoutSecret(uri));

        var responseRaw = makeRequest(uri);
        var image = responseRaw.flatMap(it -> mapToImage(it, searchQuery));

        log.info("searchImage result {}", image);

        return image;
    }

    Optional<T> makeRequest(URI uri) {
        T response = null;

        try {
            // TODO: Retry?
            response = doMakeRequest(uri);
        } catch (Exception e) {
            log.error("could not get response", e);
        }

        return Optional.ofNullable(response);
    }

    abstract T doMakeRequest(URI uri);

    abstract Optional<ImageSearchClientImageDto> mapToImage(T responseRaw, String searchQuery);

    abstract URI buildUri(String searchQuery);

    private String withoutSecret(URI uri) {
        return uri.toString().replace(getApiKey(), "SECRET");
    }

    abstract String getApiKey();
}
