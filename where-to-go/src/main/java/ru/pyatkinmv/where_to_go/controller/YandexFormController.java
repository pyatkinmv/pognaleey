package ru.pyatkinmv.where_to_go.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import ru.pyatkinmv.where_to_go.dto.YandexFormRequestDto;
import ru.pyatkinmv.where_to_go.dto.GenerateTravelOptionsResponseDto;
import ru.pyatkinmv.where_to_go.service.GenerateTravelOptionsService;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
public class YandexFormController {
    private final GenerateTravelOptionsService generateTravelOptionsService;

    @PostMapping(value = "/generate-travel-options", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<GenerateTravelOptionsResponseDto> generateTravelOptions(@RequestBody YandexFormRequestDto request) {
        return ResponseEntity.ok(generateTravelOptionsService.generateTravelOptions(request));
    }

    @PostMapping(value = "/submit", produces = APPLICATION_JSON_VALUE)
//    public ResponseEntity<String> submit(@RequestBody String text) {
//        return ResponseEntity.ok()
//    }
    public ModelAndView getSubmitPage() {
        ModelAndView modelAndView = new ModelAndView("submit"); // Указывает на submit.html в папке templates
        return modelAndView;
    }


    @GetMapping
    public ResponseEntity<GenerateTravelOptionsResponseDto> test() {
        return ResponseEntity.ok(generateTravelOptionsService.generateTravelOptions(
                new YandexFormRequestDto("Hello!", ZonedDateTime.now().toInstant(), "answerId", "userId"
                        , Map.of("q1", List.of("q1a1"))
                )
        ));
    }

//    @GetMapping("/")
//    public String home(Model model) {
//        model.addAttribute("name", "World");
//        return "index"; // Указывает на файл templates/index.html
//    }
}