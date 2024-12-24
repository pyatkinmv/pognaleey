package ru.pyatkinmv.pognaleey.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PromptServiceTest {

    @Test
    void getShortPrompt() {
        var actual = PromptService.getShortPrompt(5, "budget=100&purpose=fun");
        var expected = "Придумай мне ровно 5 варианта путешествий исходя из входных условий." +
                "Ответ выдай в формате: место1;описание|место2;описание|место3;описание." +
                "Описание не должно содержать более 5 слов." +
                "Пример: Париж;город любви и романтики|Рим;вечный город с богатой историей|Барселона;история и современность." +
                "Не надо никаких дополнительных нумераций и слов, ответ просто одной строкой на русском языке." +
                "Условия: budget=100&purpose=fun";
        assertThat(expected).isEqualTo(actual);
    }

    @Test
    void getDetailedPrompt() {
        var now = Instant.now();
        var recommendations = List.of(
                new TravelRecommendation(1L, 1L, "t1", "d1", "dt1", "i1", now),
                new TravelRecommendation(2L, 1L, "t2", "d2", "dt2", "i2", now),
                new TravelRecommendation(3L, 1L, "t3", "d3", "dt3", "i3", now)
        );
        var actual = PromptService.getDetailedPrompt(recommendations, "budget=500&purpose=romantic");
        var expected = "У меня есть следующие 3 варианта для путешествия: 1)t1—d1;2)t2—d2;3)t3—d3." +
                "Мои пожелания для путешествия следующие: budget=500&purpose=romantic." +
                "Дай мне исходя из этих предпочтений подробное описание этих вариантов в формате JSON" +
                "(не надо никаких дополнительных нумераций и слов, в ответе только JSON)." +
                "Формат:" +
                "{recommendations: [  " +
                "{title: НАЗВАНИЕ МЕСТА," +
                "budget: {from: БЮДЖЕТ ОТ, to: БЮДЖЕТ ДО}," +
                "reasoning: ПОЧЕМУ ЭТОТ ВАРИАНТ ПОДХОДИТ," +
                "creativeDescription: КРЕАТИВНОЕ ХУДОЖЕСТВЕННОЕ ОПИСАНИЕ ВАРИАНТА," +
                "tips: ОБЩИЕ РЕКОМЕНДАЦИИ (СТРОКА)," +
                "whereToGo: [ДОСТОПРИМЕЧАТЕЛЬНОСТИ/КОНКРЕТНЫЕ МЕСТА РЕКОМЕНДУЕМЫЕ ДЛЯ ПОСЕЩЕНИЯ]," +
                "additionalConsideration: ЧТО НУЖНО ДОПОЛНИТЕЛЬНО УЧЕСТЬ  } " +
                "]" +
                "}";
        assertThat(expected).isEqualTo(actual);
    }
}