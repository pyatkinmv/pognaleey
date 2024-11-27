package ru.pyatkinmv.where_to_go.mapper;

import ru.pyatkinmv.where_to_go.dto.YandexFormRequestDto;
import ru.pyatkinmv.where_to_go.dto.GenerateTravelOptionsResponseDto;
import ru.pyatkinmv.where_to_go.dto.GptGenerateTravelOptionsRequestDto;
import ru.pyatkinmv.where_to_go.dto.GptGenerateTravelOptionsResponseDto;

public class TravelOptionsMapper {

    public static GptGenerateTravelOptionsRequestDto map(YandexFormRequestDto request) {
        return new GptGenerateTravelOptionsRequestDto();
    }

    public static GenerateTravelOptionsResponseDto map(GptGenerateTravelOptionsResponseDto response) {
        return new GenerateTravelOptionsResponseDto(response.html());
    }
}
