package ru.pyatkinmv.pognaleey.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;
import ru.pyatkinmv.pognaleey.client.ChatGptHttpClient;
import ru.pyatkinmv.pognaleey.client.GptHttpClient;
import ru.pyatkinmv.pognaleey.client.RateLimiter;
import ru.pyatkinmv.pognaleey.client.YandexGptHttpClient;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder().build();
    }

    @Bean
    public RestTemplate restTemplateWithTimeout(RestTemplateBuilder builder) {
        return builder.connectTimeout(Duration.ofMillis(1500)).readTimeout(Duration.ofSeconds(3)).build();
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
            @Value("${gpt-client.yandex.folder-id}") String gptFolderId
    ) {
        return new YandexGptHttpClient(restTemplate, gptBaseUrl, modelUri, gptApiKey, gptFolderId);
    }

    @Bean
    @ConditionalOnProperty(prefix = "gpt-client", name = "current", havingValue = "openai")
    public GptHttpClient chatGptHttpClient(
            @Autowired RestTemplate restTemplate,
            @Value("${gpt-client.openai.base-url}") String gptBaseUrl,
            @Value("${gpt-client.openai.model}") String model,
            @Value("${gpt-client.openai.api-key}") String gptApiKey
    ) {
        return new ChatGptHttpClient(restTemplate, gptBaseUrl, model, gptApiKey);
    }

    @Bean
    public RateLimiter imagesSearchClientRateLimiter(
            @Value("${image-search-client.permits-per-second}") double permitsPerSecond) {
        return new RateLimiter(permitsPerSecond);
    }
}
