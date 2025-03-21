package ru.pyatkinmv.pognaleey.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.scheduling.annotation.AsyncConfigurer;

@Configuration
public class TestAsyncConfig implements AsyncConfigurer {

  @Bean
  public Executor taskExecutor() {
    return Runnable::run;
  }

  @Bean
  public ExecutorService executorService() {
    return new ExecutorServiceAdapter(new SyncTaskExecutor());
  }

  @Bean
  public Executor asyncExecutor() {
    return Runnable::run;
  }
}
