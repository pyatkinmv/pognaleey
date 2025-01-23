package ru.pyatkinmv.pognaleey.client;

import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.pyatkinmv.pognaleey.dto.PixabayImagesResponseDto;
import ru.pyatkinmv.pognaleey.dto.SearchImageDto;

import java.net.URI;
import java.util.Optional;

@RequiredArgsConstructor
public class PixabayImageSearchHttpClient extends ImageSearchHttpClient<PixabayImagesResponseDto> {
    private final RestTemplate restTemplate;

    private final String imageSearchApiKey;
    private final String imageSearchBaseUrl;

    @Override
    PixabayImagesResponseDto doMakeRequest(URI uri) {
        return restTemplate.getForObject(uri, PixabayImagesResponseDto.class);
    }

    @Override
    Optional<SearchImageDto> mapToImage(PixabayImagesResponseDto response) {
        return Optional.of(response.hits().getFirst())
                .map(it -> new SearchImageDto(it.webformatURL(), it.webformatURL()));
    }

    @Override
    URI buildUri(String searchQuery) {
        return UriComponentsBuilder.fromUriString(imageSearchBaseUrl)
                .queryParam("key", imageSearchApiKey)  // Добавление параметров
                .queryParam("image_type", "photo")
                .queryParam("orientation", "horizontal")
                .queryParam("min_width", "800")
                .queryParam("per_page", 3)
                .queryParam("q", searchQuery)
                .build()
                .encode()
                .toUri();
    }

    @Override
    String getApiKey() {
        return imageSearchApiKey;
    }
}
