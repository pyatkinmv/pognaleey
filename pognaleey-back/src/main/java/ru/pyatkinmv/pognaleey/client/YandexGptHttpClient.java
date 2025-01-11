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
public class YandexGptHttpClient implements GptHttpClient {
    private final RestTemplate restTemplate;

    private final String gptBaseUrl;
    private final String modelUri;
    private final String gptApiKey;
    private final String gptFolderId;

    @Override
    public String ask(String prompt) {
        log.info("question: {}", prompt);
        var requestEntity = buildRequest(prompt);
        var response = restTemplate.exchange(gptBaseUrl, HttpMethod.POST, requestEntity, YandexGptBodyResponseDto.class);
        var responseBody = Optional.ofNullable(response.getBody())
                .orElseThrow(() -> new RuntimeException("Could not get answer from gpt client"));

        if (responseBody.result().isModelVersionUpdated()) {
            log.warn("model version updated to {}", responseBody.result().modelVersion());
        }
        log.info("response: {}", responseBody);

        return responseBody.result().alternatives().getFirst().message().text();
    }

    private HttpEntity<YandexGptBodyRequestDto> buildRequest(String prompt) {
        var request = new YandexGptBodyRequestDto(prompt, modelUri);
        var headers = new HttpHeaders();
        headers.add("x-folder-id", gptFolderId);
        headers.add(HttpHeaders.AUTHORIZATION, "Api-Key " + gptApiKey);

        return new HttpEntity<>(request, headers);
    }

    private record YandexGptBodyRequestDto(String modelUri, CompletionOptionsDto completionOptions,
                                           List<MessageDto> messages) {

        public YandexGptBodyRequestDto(String text, String modelUri) {
            this(modelUri,
                    new CompletionOptionsDto(false, 0.2D, "1500"),
                    List.of(new MessageDto("user", text)));
        }

        record CompletionOptionsDto(boolean stream, double temperature, String maxTokens) {
        }

        record MessageDto(String role, String text) {
        }
    }

    private record YandexGptBodyResponseDto(Result result) {

        public record Result(List<Alternative> alternatives, String modelVersion) {
            public boolean isModelVersionUpdated() {
                return !"23.10.2024".equals(modelVersion);
            }
        }

        public record Alternative(Message message) {

        }

        public record Message(String role, String text) {
        }
    }
}
