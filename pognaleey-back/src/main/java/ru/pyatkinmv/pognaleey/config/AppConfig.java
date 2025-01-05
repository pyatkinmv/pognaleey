package ru.pyatkinmv.pognaleey.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

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
        int nThreads = Runtime.getRuntime().availableProcessors();
        log.info("nThreads={}", nThreads);

        return Executors.newFixedThreadPool(nThreads);
    }

}
