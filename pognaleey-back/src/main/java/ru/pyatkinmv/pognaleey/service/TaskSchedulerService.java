package ru.pyatkinmv.pognaleey.service;

import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskSchedulerService {
  private final TaskScheduler taskScheduler;

  public <T> void scheduleTask(Callable<T> task, int delayInSeconds) {
    var future = new CompletableFuture<T>();
    var startTime = Instant.now().plusSeconds(delayInSeconds);
    taskScheduler.schedule(
        () -> {
          try {
            T result = task.call();
            future.complete(result);
          } catch (Exception e) {
            future.completeExceptionally(e);
          }
        },
        startTime);
  }
}
