package ru.pyatkinmv.pognaleey.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.function.Supplier;

@Slf4j
public final class Utils {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    public static <T> T measuringTime(Supplier<T> runnable) {
        long start = Instant.now().toEpochMilli();
        log.info("start at {}", start);

        var result = runnable.get();

        long end = Instant.now().toEpochMilli();
        log.info("end at {}, total ms {}", end, end - start);

        return result;
    }

    @SneakyThrows
    public static String toJson(Object obj) {
        return OBJECT_MAPPER.writeValueAsString(obj);
    }
}
