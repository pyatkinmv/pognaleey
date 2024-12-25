package ru.pyatkinmv.pognaleey.dto.gpt;

import java.util.List;

public interface HasRecommendations<T> {
    List<T> recommendations();
}
