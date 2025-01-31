package ru.pyatkinmv.pognaleey.config;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import ru.pyatkinmv.pognaleey.client.*;
import ru.pyatkinmv.pognaleey.dto.FlickrImagesResponseDto;
import ru.pyatkinmv.pognaleey.dto.OpenverseImagesResponseDto;
import ru.pyatkinmv.pognaleey.dto.PixabayImagesResponseDto;

@Slf4j
@Configuration
@EnableScheduling
public class AppConfig {

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplateBuilder().build();
  }

  @Bean
  public RestTemplate restTemplateWithTimeout(RestTemplateBuilder builder) {
    return builder
        .connectTimeout(Duration.ofMillis(1500))
        .readTimeout(Duration.ofSeconds(3))
        .build();
  }

  @Profile("!test")
  @Bean
  public ExecutorService executorService() {
    log.info("using cachedThreadPoolExecutor");

    return Executors.newCachedThreadPool();
  }

  @Bean
  @ConditionalOnProperty(prefix = "gpt-client", name = "current", havingValue = "yandex")
  public GptHttpClient yandexGptHttpClient(
      @Autowired RestTemplate restTemplate,
      @Value("${gpt-client.yandex.base-url}") String gptBaseUrl,
      @Value("${gpt-client.yandex.model-uri}") String modelUri,
      @Value("${gpt-client.yandex.api-key}") String gptApiKey,
      @Value("${gpt-client.yandex.folder-id}") String gptFolderId) {
    return new YandexGptHttpClient(restTemplate, gptBaseUrl, modelUri, gptApiKey, gptFolderId);
  }

  @Bean
  @ConditionalOnProperty(prefix = "gpt-client", name = "current", havingValue = "openai")
  public GptHttpClient chatGptHttpClient(
      @Autowired RestTemplate restTemplate,
      @Value("${gpt-client.openai.base-url}") String gptBaseUrl,
      @Value("${gpt-client.openai.model}") String model,
      @Value("${gpt-client.openai.api-key}") String gptApiKey) {
    return new ChatGptHttpClient(restTemplate, gptBaseUrl, model, gptApiKey);
  }

  @Bean
  @ConditionalOnProperty(prefix = "image-search-client", name = "current", havingValue = "pixabay")
  public ImageSearchHttpClient<PixabayImagesResponseDto> pixabayImageSearchHttpClient(
      @Autowired RestTemplate restTemplate,
      @Value("${image-search-client.pixabay.api-key}") String apiKey,
      @Value("${image-search-client.pixabay.base-url}") String baseUrl) {
    return new PixabayImageSearchHttpClient(restTemplate, apiKey, baseUrl);
  }

  @Bean
  @ConditionalOnProperty(
      prefix = "image-search-client",
      name = "current",
      havingValue = "openverse")
  public ImageSearchHttpClient<OpenverseImagesResponseDto> openverseImageSearchHttpClient(
      @Autowired RestTemplate restTemplate,
      @Value("${image-search-client.openverse.base-url}") String baseUrl) {
    return new OpenverseImageSearchHttpClient(restTemplate, baseUrl);
  }

  @Bean
  @ConditionalOnProperty(prefix = "image-search-client", name = "current", havingValue = "flickr")
  public ImageSearchHttpClient<FlickrImagesResponseDto> flickrImageSearchHttpClient(
      @Autowired RestTemplate restTemplate,
      @Value("${image-search-client.flickr.api-key}") String apiKey,
      @Value("${image-search-client.flickr.base-url}") String baseUrl) {
    return new FlickrImageSearchHttpClient(restTemplate, apiKey, baseUrl);
  }

  @Bean
  @ConditionalOnProperty(prefix = "image-search-client", name = "current", havingValue = "yandex")
  public ImageSearchHttpClient<String> yandexImageSearchHttpClient(
      @Autowired RestTemplate restTemplate,
      @Autowired RestTemplate restTemplateWithTimeout,
      @Value("${image-search-client.yandex.api-key}") String apiKey,
      @Value("${image-search-client.yandex.folder-id}") String folderId,
      @Value("${image-search-client.yandex.base-url}") String baseUrl,
      @Value("${image-search-client.yandex.permits-per-second}") double permitsPerSecond) {
    return new YandexImageSearchHttpClient(
        restTemplate,
        restTemplateWithTimeout,
        new RateLimiter(permitsPerSecond),
        apiKey,
        folderId,
        baseUrl);
  }

  @Bean
  public KandinskyImageGenerateHttpClient kandinskyImageGenerateHttpClient(
      @Autowired RestTemplate restTemplate) {
    return new KandinskyImageGenerateHttpClient(restTemplate);
  }
}
