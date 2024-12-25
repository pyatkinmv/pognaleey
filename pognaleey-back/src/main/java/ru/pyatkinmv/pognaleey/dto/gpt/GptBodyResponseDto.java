package ru.pyatkinmv.pognaleey.dto.gpt;

import java.util.List;

public record GptBodyResponseDto(Result result) {

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