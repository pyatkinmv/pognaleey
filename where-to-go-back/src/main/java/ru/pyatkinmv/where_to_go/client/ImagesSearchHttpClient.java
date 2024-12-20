package ru.pyatkinmv.where_to_go.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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

    private static final String URL_FORMAT = "https://yandex.ru/images-xml?apikey=APIKEY&folderid=FOLDER_ID&text=%s&isize=large&groupby=attr=ii.groups-on-page=1&itype=png&iorient=square";
    //&iorient=square

    public String searchImageUrl(String text) {
        log.info("searchImageUrl for text {}", text);

        var headers = new HttpHeaders();
//        headers.add("Content-Type", "text/plain; charset=UTF-8");
        var requestEntity = new HttpEntity<>(headers);
//        var url = UriComponentsBuilder.fromUriString("https://yandex.ru/images-xml")
//                .queryParam("text", text)
//                .queryParam("folderid", "FOLDER_ID")
////                .queryParam("groupby", "attr=ii.groups-on-page=1")
//                .queryParam("iorient", "square")
//                .queryParam("apikey", "APIKEY")
//                .queryParam("isize", "large")
//                .build(false)
////                .encode(StandardCharsets.UTF_8)
//                .toUriString();

        var url = String.format(URL_FORMAT, text);

        log.info("searchImageUrl url {}", url);
        var responseXml = defaultRestTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
//        log.info("searchImageUrl responseXml {}", responseXml);

        // Регулярное выражение для поиска <image-link>
        String regex = "<image-link>(.*?)</image-link>";

        // Создаем Pattern и Matcher
        var pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(responseXml.getBody());

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
            log.info(responseXml.getBody());
        }

        return imageLinks.getFirst();
    }
}
