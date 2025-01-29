package ru.pyatkinmv.pognaleey.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class TaskSchedulerService {
    private final TaskScheduler taskScheduler;

    public <T> CompletableFuture<T> scheduleTask(Callable<T> task, int delayInSeconds) {
        CompletableFuture<T> future = new CompletableFuture<>();

        // Планируем выполнение задачи через указанное время
        Instant startTime = Instant.now().plusSeconds(delayInSeconds);
        taskScheduler.schedule(() -> {
            try {
                // Выполняем задачу и завершаем CompletableFuture
                T result = task.call();
                future.complete(result);
            } catch (Exception e) {
                // Завершаем с исключением в случае ошибки
                future.completeExceptionally(e);
            }
        }, startTime);

        return future;
    }
}
