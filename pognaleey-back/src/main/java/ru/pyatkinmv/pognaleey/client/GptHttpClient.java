package ru.pyatkinmv.pognaleey.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.pyatkinmv.pognaleey.dto.gpt.GptBodyRequestDto;
import ru.pyatkinmv.pognaleey.dto.gpt.GptBodyResponseDto;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GptHttpClient {
    private final RestTemplate restTemplate;

    @Value("${gpt-client.base-url}")
    private String gptBaseUrl;

    @Value("${gpt-client.model-uri}")
    private String modelUri;

    @Value("${gpt-client.api-key}")
    private String gptApiKey;

    @Value("${gpt-client.folder-id}")
    private String gptFolderId;

    public String ask(String prompt) {
        log.info("question: {}", prompt);
        var requestEntity = buildRequest(prompt);
        var response = restTemplate.exchange(gptBaseUrl, HttpMethod.POST, requestEntity, GptBodyResponseDto.class);
        var responseBody = Optional.ofNullable(response.getBody())
                .orElseThrow(() -> new RuntimeException("Could not get answer from gpt client"));

        if (responseBody.result().isModelVersionUpdated()) {
            log.warn("model version updated to {}", responseBody.result().modelVersion());
        }

        var answer = responseBody.result().alternatives().getFirst().message().text();
        log.info("answer: {}", answer);

        return answer;
    }

    private HttpEntity<GptBodyRequestDto> buildRequest(String prompt) {
        var request = new GptBodyRequestDto(prompt, modelUri);
        var headers = new HttpHeaders();
        headers.add("x-folder-id", gptFolderId);
        headers.add(HttpHeaders.AUTHORIZATION, "Api-Key " + gptApiKey);

        return new HttpEntity<>(request, headers);
    }

}
