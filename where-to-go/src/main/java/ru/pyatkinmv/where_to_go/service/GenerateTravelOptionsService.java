package ru.pyatkinmv.where_to_go.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.pyatkinmv.where_to_go.client.GptHttpClient;
import ru.pyatkinmv.where_to_go.dto.YandexFormRequestDto;
import ru.pyatkinmv.where_to_go.dto.GenerateTravelOptionsResponseDto;
import ru.pyatkinmv.where_to_go.mapper.TravelOptionsMapper;

@Service
@RequiredArgsConstructor
public class GenerateTravelOptionsService {
    private final GptHttpClient gptHttpClient;

    public GenerateTravelOptionsResponseDto generateTravelOptions(YandexFormRequestDto request) {
        var gptRequest = TravelOptionsMapper.map(request);
        var gptResponse = gptHttpClient.generateTravelOptions(gptRequest);

        return TravelOptionsMapper.map(gptResponse);
    }

}