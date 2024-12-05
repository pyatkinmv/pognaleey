package ru.pyatkinmv.where_to_go.util;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.function.Supplier;

@Slf4j
public final class Utils {

    public static <T> T measuringTime(Supplier<T> runnable) {
        long start = Instant.now().toEpochMilli();
        log.info("start at {}", start);

        var result = runnable.get();

        long end = Instant.now().toEpochMilli();
        log.info("end at {}, total ms {}", end, end - start);

        return result;
    }
}
