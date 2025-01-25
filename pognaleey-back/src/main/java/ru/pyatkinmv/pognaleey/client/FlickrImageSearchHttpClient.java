package ru.pyatkinmv.pognaleey.client;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.pyatkinmv.pognaleey.dto.FlickrImagesResponseDto;
import ru.pyatkinmv.pognaleey.dto.ImageSearchClientImageDto;

import java.net.URI;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class FlickrImageSearchHttpClient extends ImageSearchHttpClient<FlickrImagesResponseDto> {
    private final RestTemplate restTemplate;

    private final String imageSearchApiKey;
    private final String imageSearchBaseUrl;

    private static boolean withMediumAndLargeUrl(FlickrImagesResponseDto.Photo photo) {
        return photo.url_l() != null && photo.url_m() != null;
    }

    private static Function<FlickrImagesResponseDto.Photo, Boolean> horizontalLast() {
        return (FlickrImagesResponseDto.Photo photo) -> {
            if (photo.width_m() != null && photo.height_m() != null) {
                return photo.width_m() >= photo.height_m();
            }
            return false;
        };
    }

    @Override
    FlickrImagesResponseDto doMakeRequest(URI uri) {
        return restTemplate.getForObject(uri, FlickrImagesResponseDto.class);
    }

    @Override
    Optional<ImageSearchClientImageDto> retrieveTargetImages(FlickrImagesResponseDto response, String searchQuery) {
        return response.photos().photo().stream()
                .filter(FlickrImageSearchHttpClient::withMediumAndLargeUrl)
                .max(Comparator.comparing(horizontalLast()))
                .map(it -> new ImageSearchClientImageDto(it.url_l(), it.url_m(), searchQuery));
    }

    @Override
    URI buildUri(String searchQuery) {
        return UriComponentsBuilder.fromUriString(imageSearchBaseUrl)
                .queryParam("method", "flickr.photos.search")
                .queryParam("api_key", imageSearchApiKey)
                .queryParam("text", searchQuery)
                .queryParam("license", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .queryParam("extras", "url_l,url_m,license,owner_name")
                .queryParam("format", "json")
                .queryParam("nojsoncallback", "1")
                .queryParam("per_page", 20)
                .build()
                .encode()
                .toUri();
    }

    @Override
    String getApiKey() {
        return imageSearchApiKey;
    }

    @Override
    protected boolean enableShorteningSearchQueryHack() {
        return true;
    }

}
