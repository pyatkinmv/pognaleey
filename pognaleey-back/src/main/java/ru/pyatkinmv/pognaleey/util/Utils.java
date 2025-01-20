package ru.pyatkinmv.pognaleey.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@UtilityClass
public final class Utils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @SneakyThrows
    public static String toJson(Object obj) {
        return OBJECT_MAPPER.writeValueAsString(obj);
    }

    @SneakyThrows
    public static <T> T toObject(String json, Class<T> clazz) {
        return OBJECT_MAPPER.readValue(json, clazz);
    }

    public static <T> T measuringTime(Supplier<T> runnable) {
        long start = Instant.now().toEpochMilli();
        log.info("start at {}", start);

        var result = runnable.get();

        long end = Instant.now().toEpochMilli();
        log.info("end at {}, total ms {}", end, end - start);

        return result;
    }

    public static void writeFile(String content, long guide) {
        var uuid = UUID.randomUUID();
        var fileName = String.format("guide-for-guide-%d-%s.md", guide, uuid);

        try {
            // Путь к папке resources
            Path resourcePath = Paths.get("pognaleey-back/ignorable");
            // Создаем объект файла
            File file = new File(resourcePath.toFile(), fileName);

            // Проверяем, существует ли папка resources
            if (!resourcePath.toFile().exists()) {
                throw new IllegalStateException("Папка resources не найдена: " + resourcePath.toAbsolutePath());
            }

            // Создаем файл (если он не существует)
            if (!file.exists()) {
                file.createNewFile();
            }

            // Записываем содержимое в файл
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
                System.out.println("Файл успешно создан и записан: " + file.getAbsolutePath());
            }

        } catch (IOException | RuntimeException e) {
            System.err.println("Ошибка при записи файла: " + e.getMessage());
        }
    }

    public static <T> Optional<T> get(Collection<T> collection, int i) {
        if (i < collection.size()) {
            return Optional.of(collection.stream().toList().get(i));
        } else {
            return Optional.empty();
        }
    }

    public static <T, R> List<R> extracting(Collection<T> collection, Function<T, R> function) {
        return collection.stream().map(function).toList();
    }

    public static <T> T peek(Runnable runnable, T obj) {
        runnable.run();
        return obj;
    }
}
