package ru.pyatkinmv.pognaleey.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImagesSearchHttpClient {
    private final RestTemplate defaultRestTemplate;

    @Value("${image-search-client.api-key}")
    private String imageSearchApiKey;

    @Value("${image-search-client.folder-id}")
    private String imageSearchFolderId;

    private static final String URL_FORMAT = "https://yandex.ru/images-xml?apikey=%s&folderid=%s&text=%s&isize=large" +
            "&groupby=attr=ii.groups-on-page=1&itype=png&iorient=square";

    public String searchImageUrl(String text) {
        log.info("searchImageUrl for text {}", text);

        var url = String.format(URL_FORMAT, imageSearchApiKey, imageSearchFolderId, text);

        log.info("searchImageUrl url {}", url);
        var responseXml = defaultRestTemplate.getForObject(url, String.class);

        // Регулярное выражение для поиска <image-link>
        String regex = "<image-link>(.*?)</image-link>";

        // Создаем Pattern и Matcher
        var pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(responseXml);

        // Список для хранения ссылок
        List<String> imageLinks = new ArrayList<>();

        // Поиск всех совпадений
        while (matcher.find()) {
            imageLinks.add(matcher.group(1)); // Группа 1 содержит текст внутри <image-link>
        }

        // Вывод результата
        log.info("found imageLinks {}", imageLinks);

        if (imageLinks.isEmpty()) {
            log.info("no imageLinks found");
            log.info(responseXml);
        }

        return imageLinks.getFirst();
    }
}
