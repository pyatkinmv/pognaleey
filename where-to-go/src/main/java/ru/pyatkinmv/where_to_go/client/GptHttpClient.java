package ru.pyatkinmv.where_to_go.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.pyatkinmv.where_to_go.dto.GptGenerateTravelOptionsRequestDto;
import ru.pyatkinmv.where_to_go.dto.GptGenerateTravelOptionsResponseDto;

@Component
@RequiredArgsConstructor
public class GptHttpClient {
    private final RestTemplate restTemplate;

    public GptGenerateTravelOptionsResponseDto generateTravelOptions(GptGenerateTravelOptionsRequestDto request) {
        return new GptGenerateTravelOptionsResponseDto("GptGenerateTravelOptionsResponse");
    }
}
