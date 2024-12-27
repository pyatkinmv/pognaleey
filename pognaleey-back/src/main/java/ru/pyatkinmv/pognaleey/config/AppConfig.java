package ru.pyatkinmv.pognaleey.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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

    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(15);
    }

}
