package ru.pyatkinmv.pognaleey.service;

import org.junit.jupiter.api.Test;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PromptServiceTest {

    @Test
    void generateQuickPrompt() {
        var actual = PromptService.generateQuickPrompt(3,
                "from=Moscow;to=Asia"
        );
        var expected = "Придумай мне ровно 3 варианта путешествий исходя из входных условий: from=Moscow;to=Asia." +
                "Ответ выдай в формате: место1;описание|место2;описание|место3;описание." +
                "Описание не должно содержать более 4 слов." +
                "Пример: Париж;город любви и романтики|Рим;город с богатой историей|Барселона;история и современность";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void generateDetailedPrompt() {
        var now = Instant.now();
        var recommendation =
                new TravelRecommendation(1L, now, 1L, "t1", "d1", "dt1", "i1");

        var actual = PromptService.generateDetailedPrompt(recommendation, "budget=500&purpose=romantic");
        var expected = "У меня есть идея для путешествия:t1—d1." +
                "Мои пожелания следующие: budget=500&purpose=romantic." +
                "Дай мне исходя из этих предпочтений подробное описание в формате JSON" +
                "(не надо никаких дополнительных нумераций и слов, в ответе только JSON). " +
                "Формат: {\"title\":\"НАЗВАНИЕ МЕСТА\",\"budget\":\"НЕОБХОДИМЫЙ БЮДЖЕТ (НАПРИМЕР, 1000$)\"," +
                "\"reasoning\":\"ПОЧЕМУ ЭТОТ ВАРИАНТ ПОДХОДИТ\"," +
                "\"creativeDescription\":\"КРЕАТИВНОЕ ХУДОЖЕСТВЕННОЕ ОПИСАНИЕ ВАРИАНТА\"," +
                "\"tips\":\"ОБЩИЕ РЕКОМЕНДАЦИИ (СТРОКА)\"," +
                "\"whereToGo\":[\"ДОСТОПРИМЕЧАТЕЛЬНОСТИ/КОНКРЕТНЫЕ МЕСТА РЕКОМЕНДУЕМЫЕ ДЛЯ ПОСЕЩЕНИЯ\"]," +
                "\"additionalConsideration\":\"ЧТО НУЖНО ДОПОЛНИТЕЛЬНО УЧЕСТЬ\"}";
        assertThat(actual).isEqualTo(expected);
    }
}