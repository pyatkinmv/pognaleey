package ru.pyatkinmv.pognaleey.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.pyatkinmv.pognaleey.dto.SearchImageDto;

import java.net.URI;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public abstract class ImageSearchHttpClientBase<T> {


    public Optional<SearchImageDto> searchImage(String searchQuery) {
        log.info("searchImage for searchQuery {}", searchQuery);

        if (searchQuery.isEmpty()) {
            log.info("searchQuery is empty");

            return Optional.empty();
        }

        var uri = buildUri(searchQuery);
        log.info("searchImage uri {}", withoutSecret(uri));

        var responseRaw = makeRequest(uri);
        var image = extractResponse(responseRaw);

        log.info("searchImage result {}", image);

        return image;
    }

    abstract T makeRequest(URI uri);

    abstract Optional<SearchImageDto> extractResponse(T responseRaw);

    abstract URI buildUri(String searchQuery);

    private String withoutSecret(URI uri) {
        return uri.toString().replace(getApiKey(), "SECRET");
    }

    abstract String getApiKey();
}
