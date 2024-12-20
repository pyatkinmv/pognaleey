package ru.pyatkinmv.pognaleey.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Configuration
public class AppConfig {
    @Bean
    public RestTemplate gptRestTemplate() {
        return new RestTemplateBuilder()
                .rootUri("https://llm.api.cloud.yandex.net/foundationModels/v1/completion")
                .defaultHeader("x-folder-id", "FOLDER_ID")
                .defaultHeader("Authorization", "Api-Key SECRET")
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
