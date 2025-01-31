package ru.pyatkinmv.pognaleey.client;

import io.jsonwebtoken.lang.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.pyatkinmv.pognaleey.dto.KandinskyGetImagesResponseDto;
import ru.pyatkinmv.pognaleey.dto.KandinskyPostImagesResponseDto;
import ru.pyatkinmv.pognaleey.util.Utils;

@Slf4j
@RequiredArgsConstructor
public class KandinskyImageGenerateHttpClient {
  private final RestTemplate restTemplate;

  private final String getUrlFormat =
      "https://api-key.fusionbrain.ai/key/api/v1/text2image/status/%s";
  private final String postUrl = "https://api-key.fusionbrain.ai/key/api/v1/text2image/run";

  public Optional<KandinskyPostImagesResponseDto> generateImage(String query) {
    log.info("generateImage for query {}", query);

    if (query.isEmpty()) {
      log.info("generateImage is empty");

      return Optional.empty();
    }

    var formData = new LinkedMultiValueMap<String, Object>();
    formData.add("model_id", "4");
    HttpHeaders jsonPartHeaders = new HttpHeaders();
    jsonPartHeaders.setContentType(MediaType.APPLICATION_JSON);
    formData.add("params", new HttpEntity<>(ParamsPost.toStr(query), jsonPartHeaders));
    // Устанавливаем заголовки
    HttpHeaders headers = buildHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    // Создаем HttpEntity для отправки запроса
    var requestEntity = new HttpEntity<>(formData, headers);

    var response =
        restTemplate.exchange(
            postUrl, HttpMethod.POST, requestEntity, KandinskyPostImagesResponseDto.class);

    if (!response.getStatusCode().is2xxSuccessful()) {
      log.error("failed to generate image, client status code={}", response.getStatusCode());
      return Optional.empty();
    }
    log.info("generate image successful {}", response.getBody());

    return Optional.ofNullable(response.getBody());
  }

  private HttpHeaders buildHeaders() {
    var headers = new HttpHeaders();
    headers.add("X-Key", "Key 41D9E0C2E1037CE580D100366DD78373");
    headers.add("X-Secret", "Secret 0F786529FB63032A36A9460B80DAB8CF");

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

    // Создаем HttpEntity для отправки запроса
    var requestEntity = new HttpEntity<>(headers);

    var response =
        restTemplate.exchange(
            String.format(getUrlFormat, uuid),
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
