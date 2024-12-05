package ru.pyatkinmv.where_to_go.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
                .rootUri("https://llm.api.cloud.yandex.net/foundationModels/v1/completion")
                .defaultHeader("x-folder-id", "FOLDER_ID")
                .defaultHeader("Authorization", "Api-Key SECRET")
                .build();
    }
}
