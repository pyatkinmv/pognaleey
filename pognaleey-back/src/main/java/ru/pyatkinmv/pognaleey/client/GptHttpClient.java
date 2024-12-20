package ru.pyatkinmv.pognaleey.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class GptHttpClient {
    private final RestTemplate gptRestTemplate;

    public static final String BODY_TEMPLATE = """
            {
              "modelUri": "gpt://b1g4e7108ae9pp9j7tn7/yandexgpt/rc",
              "completionOptions": {
                "stream": false,
                "temperature": 0.2,
                "maxTokens": "1500"
              },
              "messages": [
                {
                    "role": "user",
                    "text": "%s"
                }
              ]
            }
            """;

    public String ask(String prompt) {
        log.info("question: {}", prompt);
        var request = String.format(BODY_TEMPLATE, prompt);
        var headers = new HttpHeaders();
        headers.add("Content-Type", "text/plain; charset=UTF-8");
        var requestEntity = new HttpEntity<>(request, headers);

        var response = gptRestTemplate.exchange("https://llm.api.cloud.yandex.net/foundationModels/v1/completion",
                HttpMethod.POST, requestEntity, JsonNode.class);
        var result = response.getBody().get("result").get("alternatives").get(0).get("message").get("text").asText()
                .replace("```", "");
        log.info("answer: {}", result);

        return result;
    }

}
