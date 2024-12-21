package ru.pyatkinmv.pognaleey.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Configuration
public class AppConfig {

    @Value("${gpt-client.api-key}")
    private String gptApiKey;

    @Value("${gpt-client.folder-id}")
    private String gptFolderId;

    @Bean
    public RestTemplate gptRestTemplate() {
        return new RestTemplateBuilder()
                .rootUri("https://llm.api.cloud.yandex.net/foundationModels/v1/completion")
                .defaultHeader("x-folder-id", gptFolderId)
                .defaultHeader("Authorization", "Api-Key " + gptApiKey)
                .build();
    }

    @Bean
    public RestTemplate defaultRestTemplate() {
        return new RestTemplateBuilder().build();
    }

    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(15);
    }
}
