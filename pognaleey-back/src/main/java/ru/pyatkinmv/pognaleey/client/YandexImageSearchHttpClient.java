package ru.pyatkinmv.pognaleey.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.pyatkinmv.pognaleey.dto.SearchImageDto;

import java.net.URI;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Deprecated
public class YandexImageSearchHttpClient extends ImageSearchHttpClient<String> {
    private final RestTemplate restTemplate;
    private final RestTemplate restTemplateWithTimeout;
    private final RateLimiter rateLimiter;

    private final String imageSearchApiKey;
    private final String imageSearchFolderId;
    private final String imageSearchBaseUrl;

    @Override
    String doMakeRequest(URI uri) {
        return restTemplate.getForObject(uri, String.class);
    }

    @Override
    public Optional<SearchImageDto> searchImage(String searchQuery) {
        rateLimiter.acquire();

        return super.searchImage(searchQuery);
    }

    @Override
    Optional<SearchImageDto> mapToImage(String responseRaw) {
        // Either url or image-link must work
        var regex = "<url>(.*?)</url>|<image-link>(.*?)</image-link>";
        var pattern = Pattern.compile(regex);
        var matcher = pattern.matcher(responseRaw);
        var imageUrls = new ArrayList<String>();

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                imageUrls.add(matcher.group(1));
            }
            if (matcher.group(2) != null) {
                imageUrls.add(matcher.group(2));
            }
        }

        log.info("found imageUrls {}", imageUrls);

        if (imageUrls.isEmpty()) {
            log.error("no imageUrls found; possibly bad response");
        }

        var resultUrl = imageUrls.stream().filter(this::isUrlValid)
                .findFirst()
                .orElseGet(() -> {
                    log.error("no valid imageUrl found; response {}", responseRaw);

                    return null;
                });

        return Optional.ofNullable(resultUrl).map(it -> new SearchImageDto(it, it));
    }


    @Override
    URI buildUri(String searchQuery) {
        return UriComponentsBuilder.fromUriString(imageSearchBaseUrl)
                .queryParam("apikey", imageSearchApiKey)  // Добавление параметров
                .queryParam("folderid", imageSearchFolderId)
                .queryParam("isize", "large")
                .queryParam("groupby", "attr=ii.groups-on-page=5")
                .queryParam("text", searchQuery)
                .build()
                .encode() // Кодировка параметров
                .toUri();
    }

    @Override
    String getApiKey() {
        return imageSearchApiKey;
    }

    private boolean isUrlValid(String imageUrl) {
        ResponseEntity<?> response;

        try {
            response = restTemplateWithTimeout.exchange(imageUrl, HttpMethod.HEAD, null, Object.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                var contentLength = response.getHeaders().get(HttpHeaders.CONTENT_LENGTH);

                if (contentLength != null && !contentLength.isEmpty() && !"0".equals(contentLength.getFirst())) {
                    return true;
                }
            }
        } catch (RuntimeException ex) {
            log.warn("imageUrl {} is not valid: {}", imageUrl, ex.getMessage());

            return false;
        }

        log.warn("imageUrl {} is not valid: {}", imageUrl, response);

        return false;
    }
}
