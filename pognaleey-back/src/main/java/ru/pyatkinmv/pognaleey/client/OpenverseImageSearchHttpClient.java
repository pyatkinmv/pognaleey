package ru.pyatkinmv.pognaleey.client;

import java.net.URI;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.pyatkinmv.pognaleey.dto.ImageSearchClientImageDto;
import ru.pyatkinmv.pognaleey.dto.OpenverseImagesResponseDto;

@RequiredArgsConstructor
public class OpenverseImageSearchHttpClient
    extends ImageSearchHttpClient<OpenverseImagesResponseDto> {
  private final RestTemplate restTemplate;

  private final String imageSearchBaseUrl;

  @Override
  OpenverseImagesResponseDto doMakeRequest(URI uri) {
    return restTemplate.getForObject(uri, OpenverseImagesResponseDto.class);
  }

  @Override
  Optional<ImageSearchClientImageDto> retrieveTargetImages(
      OpenverseImagesResponseDto response, String searchQuery) {
    return response.results().stream()
        .map(
            it ->
                new ImageSearchClientImageDto(
                    it.url(),
                    it.thumbnail(),
                    searchQuery,
                    it.license_url(),
                    it.creator(),
                    it.creator_url()))
        .findFirst();
  }

  @Override
  URI buildUri(String searchQuery) {
    return UriComponentsBuilder.fromUriString(imageSearchBaseUrl)
        .queryParam("aspect_ratio", "square,wide")
        .queryParam("size", "large,medium")
        .queryParam("license_type", "all-cc")
        .queryParam("q", searchQuery)
        .build()
        .encode()
        .toUri();
  }

  @Override
  String getApiKey() {
    return "not-needed";
  }
}
