package ru.pyatkinmv.pognaleey.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class ChatGptHttpClient implements GptHttpClient {
    private final RestTemplate restTemplate;

    private final String gptBaseUrl;
    private final String model;
    private final String gptApiKey;

    @Override
    public String ask(String prompt) {
        log.info("question: {}", prompt);
        var requestEntity = buildRequest(prompt);
        var response = restTemplate.exchange(gptBaseUrl, HttpMethod.POST, requestEntity, ChatGptBodyResponseDto.class);
        var responseBody = Optional.ofNullable(response.getBody())
                .orElseThrow(() -> new RuntimeException("Could not get answer from gpt client"));

        log.info("response: {}", responseBody);
        return responseBody.choices().getFirst().message().content();
    }

    private HttpEntity<ChatGptBodyRequestDto> buildRequest(String prompt) {
        var request = new ChatGptBodyRequestDto(model, false, prompt);
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + gptApiKey);

        return new HttpEntity<>(request, headers);
    }

    private record ChatGptBodyRequestDto(String model, boolean store, List<MessageDto> messages) {
        ChatGptBodyRequestDto(String model, boolean store, String prompt) {
            this(model, store, List.of(new MessageDto("user", prompt)));
        }
    }

    record MessageDto(String role, String content) {
    }

    private record ChatGptBodyResponseDto(String id, long created, List<ChoiceDto> choices) {

        record ChoiceDto(MessageDto message) {

        }
    }
}
