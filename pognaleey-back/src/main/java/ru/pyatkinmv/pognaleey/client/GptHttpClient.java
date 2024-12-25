package ru.pyatkinmv.pognaleey.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.pyatkinmv.pognaleey.dto.gpt.GptBodyRequestDto;
import ru.pyatkinmv.pognaleey.dto.gpt.GptBodyResponseDto;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GptHttpClient {
    private final RestTemplate gptRestTemplate;

    @Value("${gpt-client.url}")
    private String gptUrl;

    @Value("${gpt-client.model-uri}")
    private String modelUri;

    public String ask(String prompt) {
        log.info("question: {}", prompt);
        var request = new GptBodyRequestDto(prompt, modelUri);
        var response = gptRestTemplate.postForEntity(gptUrl, request, GptBodyResponseDto.class);
        var responseBody = Optional.ofNullable(response.getBody())
                .orElseThrow(() -> new RuntimeException("Could not get answer from gpt client"));

        if (responseBody.result().isModelVersionUpdated()) {
            log.warn("model version updated to {}", responseBody.result().modelVersion());
        }

        var answer = responseBody.result().alternatives().getFirst().message().text();
        log.info("answer: {}", answer);

        return answer;
    }


    //Стамбул; город контрастов|Прага; столица Чехии|Тбилиси; гостеприимный город|

}
