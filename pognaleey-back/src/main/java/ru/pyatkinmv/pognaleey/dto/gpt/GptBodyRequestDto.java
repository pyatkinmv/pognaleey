package ru.pyatkinmv.pognaleey.dto.gpt;

import java.util.List;

public record GptBodyRequestDto(String modelUri, CompletionOptionsDto completionOptions, List<MessageDto> messages) {

    record CompletionOptionsDto(boolean stream, double temperature, String maxTokens) {
    }

    record MessageDto(String role, String text) {
    }

    public GptBodyRequestDto(String text, String modelUri) {
        this(modelUri,
                new CompletionOptionsDto(false, 0.2D, "1500"),
                List.of(new MessageDto("user", text)));
    }
}