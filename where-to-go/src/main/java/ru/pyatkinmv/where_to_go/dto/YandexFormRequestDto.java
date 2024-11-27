package ru.pyatkinmv.where_to_go.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record YandexFormRequestDto(
        String formVersion, Instant createdAt, String answerId, String userId,
                                   Map<String, List<String>> answers
) {


}
