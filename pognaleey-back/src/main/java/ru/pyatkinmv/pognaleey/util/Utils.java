package ru.pyatkinmv.pognaleey.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public final class Utils {

  private static final ObjectMapper OBJECT_MAPPER =
      new ObjectMapper()
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

  public static <T> Optional<T> getOrEmpty(Collection<T> collection, int i) {
    if (i < collection.size()) {
      return Optional.of(collection.stream().toList().get(i));
    } else {
      return Optional.empty();
    }
  }

  public static <T, R> List<R> extracting(Collection<T> collection, Function<T, R> function) {
    return collection.stream().map(function).toList();
  }

  public static <T> Map<String, T> toCaseInsensitiveTreeMap(
      List<T> searchableItems, Function<T, String> keyFun) {
    return searchableItems.stream()
        .collect(
            Collectors.toMap(
                keyFun, it -> it, (a, b) -> b, () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER)));
  }

  public static <T> Optional<T> tryOrEmpty(Future<T> supplier) {
    try {
      return Optional.of(supplier.get());
    } catch (Exception e) {
      log.error("couldn't get from future {}", e.getMessage());

      return Optional.empty();
    }
  }

  public static <T> T peek(Runnable runnable, T obj) {
    runnable.run();
    return obj;
  }
}
