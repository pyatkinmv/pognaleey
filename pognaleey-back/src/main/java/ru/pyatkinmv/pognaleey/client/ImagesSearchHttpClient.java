package ru.pyatkinmv.pognaleey.client;

import com.google.common.util.concurrent.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

@SuppressWarnings("UnstableApiUsage")
@Slf4j
@Component
@RequiredArgsConstructor
public class ImagesSearchHttpClient {
    private final RestTemplate restTemplate;
    private final RestTemplate restTemplateWithTimeout;
    private final RateLimiter rateLimiter;

    @Value("${image-search-client.api-key}")
    private String imageSearchApiKey;

    @Value("${image-search-client.folder-id}")
    private String imageSearchFolderId;

    @Value("${image-search-client.base-url}")
    private String imageSearchBaseUrl;

    public Optional<String> searchImageUrlWithRateLimiting(String text) {
        rateLimiter.acquire();

        return searchImageUrl(text);
    }

    private Optional<String> searchImageUrl(String text) {
        log.info("searchImageUrl for text {}", text);
        var uri = buildUri(text);
        log.info("searchImageUrl uri {}", withoutSecret(uri));
        var responseXml = Objects.requireNonNull(restTemplate.getForObject(uri, String.class));

        // Either url or image-link must work
        var regex = "<url>(.*?)</url>|<image-link>(.*?)</image-link>";
        var pattern = Pattern.compile(regex);
        var matcher = pattern.matcher(responseXml);
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

        // TODO: Fix
        var resultUrl = imageUrls.stream().filter(this::isUrlValid).findFirst().orElseGet(() -> {
            log.error("no valid imageUrl found; response {}", responseXml);

            return null;
        });

        log.info("searchImageUrl resultUrl {}", resultUrl);

        return Optional.ofNullable(resultUrl);
    }

    private URI buildUri(String text) {
        return UriComponentsBuilder.fromUriString(imageSearchBaseUrl)
                .queryParam("apikey", imageSearchApiKey)  // Добавление параметров
                .queryParam("folderid", imageSearchFolderId)
                .queryParam("isize", "large")
                .queryParam("groupby", "attr=ii.groups-on-page=5")
                .queryParam("text", text)
                .build()
                .encode() // Кодировка параметров
                .toUri();
    }

    private String withoutSecret(URI uri) {
        return uri.toString().replace(imageSearchApiKey, "SECRET");
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
