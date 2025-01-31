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
import ru.pyatkinmv.pognaleey.dto.OpenverseImagesResponseDto;

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
  @ConditionalOnProperty(prefix = "gpt-client", name = "current", havingValue = "openai")
  public GptHttpClient chatGptHttpClient(
      @Autowired RestTemplate restTemplate,
      @Value("${gpt-client.openai.base-url}") String gptBaseUrl,
      @Value("${gpt-client.openai.model}") String model,
      @Value("${gpt-client.openai.api-key}") String gptApiKey) {
    return new ChatGptHttpClient(restTemplate, gptBaseUrl, model, gptApiKey);
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
  public KandinskyImageGenerateHttpClient kandinskyImageGenerateHttpClient(
      @Autowired RestTemplate restTemplate) {
    return new KandinskyImageGenerateHttpClient(restTemplate);
  }
}
