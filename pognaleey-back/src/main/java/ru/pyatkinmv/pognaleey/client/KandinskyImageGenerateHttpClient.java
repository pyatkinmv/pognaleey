package ru.pyatkinmv.pognaleey.client;

import io.jsonwebtoken.lang.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.pyatkinmv.pognaleey.dto.KandinskyGetImagesResponseDto;
import ru.pyatkinmv.pognaleey.dto.KandinskyPostImagesResponseDto;
import ru.pyatkinmv.pognaleey.util.Utils;

@Slf4j
@RequiredArgsConstructor
public class KandinskyImageGenerateHttpClient {
  private final RestTemplate restTemplate;

  private final String baseUrlGet;
  private final String baseUrlPost;
  private final String apiKey;
  private final String apiSecret;

  public Optional<KandinskyPostImagesResponseDto> generateImage(String query) {
    log.info("generateImage for query {}", query);

    if (query.isEmpty()) {
      log.info("query is empty");

      return Optional.empty();
    }

    var requestEntity = buildRequest(query);

    var response =
        restTemplate.exchange(
            baseUrlPost, HttpMethod.POST, requestEntity, KandinskyPostImagesResponseDto.class);

    if (!response.getStatusCode().is2xxSuccessful()) {
      log.error("failed to generate image, client status code={}", response.getStatusCode());
      return Optional.empty();
    }

    log.info("generate image successful {}", response.getBody());

    return Optional.ofNullable(response.getBody());
  }

  private HttpEntity<LinkedMultiValueMap<String, Object>> buildRequest(String query) {
    var formData = new LinkedMultiValueMap<String, Object>();
    formData.add("model_id", "4");
    HttpHeaders jsonPartHeaders = new HttpHeaders();
    jsonPartHeaders.setContentType(MediaType.APPLICATION_JSON);
    formData.add("params", new HttpEntity<>(ParamsPost.toStr(query), jsonPartHeaders));
    var headers = buildHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    return new HttpEntity<>(formData, headers);
  }

  @SuppressWarnings("UastIncorrectHttpHeaderInspection")
  private HttpHeaders buildHeaders() {
    var headers = new HttpHeaders();
    headers.add("X-Key", String.format("Key %s", apiKey));
    headers.add("X-Secret", String.format("Secret %s", apiSecret));

    return headers;
  }

  public String getClientName() {
    return "Kandinsky";
  }

  public String getClientWebUrl() {
    return "https://fusionbrain.ai";
  }

  public KandinskyGetImagesResponseDto getImage(String uuid) {
    HttpHeaders headers = buildHeaders();

    var requestEntity = new HttpEntity<>(headers);

    var response =
        restTemplate.exchange(
            String.format("%s/%s", baseUrlGet, uuid),
            HttpMethod.GET,
            requestEntity,
            KandinskyGetImagesResponseDto.class);

    if (!response.getStatusCode().is2xxSuccessful()) {
      log.error("failed to get image, client status code={}", response.getStatusCode());
    }

    if (response.getBody() == null || Collections.isEmpty(response.getBody().images())) {
      log.error("failed to get image, response body is empty {}", response.getBody());
    }

    return response.getBody();
  }

  record ParamsPost(String type, GenerateParams generateParams) {
    public static String toStr(String query) {
      return Utils.toJson(new ParamsPost("GENERATE", new GenerateParams(query)));
    }

    record GenerateParams(String query) {}
  }
}
