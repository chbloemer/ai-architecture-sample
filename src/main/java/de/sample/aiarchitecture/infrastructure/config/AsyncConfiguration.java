package de.sample.aiarchitecture.infrastructure.config;

import java.util.concurrent.Executor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.jspecify.annotations.NonNull;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuration for asynchronous execution.
 *
 * <p>Enables {@code @Async} annotation processing and provides a custom thread pool
 * for asynchronous initialization tasks triggered by {@link
 * de.sample.aiarchitecture.sharedkernel.common.annotation.AsyncInitialize}.
 *
 * <p><b>Architecture Pattern:</b> This is infrastructure configuration (Spring-specific).
 * The {@code @AsyncInitialize} annotation itself (in sharedkernel) is framework-agnostic.
 *
 * <p><b>Thread Pool Configuration:</b>
 * <ul>
 *   <li>Core Pool Size: 2 threads</li>
 *   <li>Max Pool Size: 10 threads</li>
 *   <li>Queue Capacity: 500 tasks</li>
 *   <li>Thread Name Prefix: "AsyncInit-"</li>
 * </ul>
 *
 * @see de.sample.aiarchitecture.sharedkernel.common.annotation.AsyncInitialize
 * @see AsyncInitializationProcessor
 */
@Configuration
@EnableAsync
public class AsyncConfiguration implements AsyncConfigurer {

  @Override
  
  public Executor getAsyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("AsyncInit-");
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(60);
    executor.initialize();
    return executor;
  }

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return (throwable, method, params) -> {
      System.err.println(
          "Uncaught async exception in method: "
              + method.getName()
              + " - "
              + throwable.getMessage());
      throwable.printStackTrace();
    };
  }
}
