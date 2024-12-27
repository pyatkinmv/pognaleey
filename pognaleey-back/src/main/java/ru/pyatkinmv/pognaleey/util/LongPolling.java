package ru.pyatkinmv.pognaleey.util;

import lombok.SneakyThrows;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LongPolling<T> {

    /**
     * Выполняет long polling.
     *
     * @param task           Задача, которая будет проверять условие и возвращать результат при успехе.
     * @param timeoutMs      Таймаут в миллисекундах.
     * @param pollIntervalMs Интервал проверки в миллисекундах.
     * @return Результат типа T, если условие завершения выполнено.
     */
    @SneakyThrows
    public T execute(Callable<Optional<T>> task, long timeoutMs, long pollIntervalMs) {
        var future = CompletableFuture.supplyAsync(() -> {
            try {
                while (true) {
                    Optional<T> result = task.call();

                    if (result.isPresent()) {
                        return result;
                    }

                    TimeUnit.MILLISECONDS.sleep(pollIntervalMs);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException("Polling task failed", e);
            }
        });

        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS).orElseThrow();
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new RuntimeException("Timeout exceeded", e);
        }
    }
}
