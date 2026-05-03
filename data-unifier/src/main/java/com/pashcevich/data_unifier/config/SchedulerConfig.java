package com.pashcevich.data_unifier.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
@EnableScheduling
@ConditionalOnProperty(
        name = "app.scheduler.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class SchedulerConfig implements SchedulingConfigurer {

    private static final int POOL_SIZE = 5;
    private static final int AWAIT_TERMINATION_SECONDS = 30;
    private static final String THREAD_NAME_PREFIX = "daa-unifier-scheduler-";

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        scheduler.setPoolSize(POOL_SIZE);
        scheduler.setThreadNamePrefix(THREAD_NAME_PREFIX);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(AWAIT_TERMINATION_SECONDS);
        scheduler.setErrorHandler(throwable ->
                log.error("Unexpected error in scheduled task", throwable));
        scheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        scheduler.initialize();
        log.info("Task scheduler initialized with pool size: {}", scheduler.getPoolSize());

        return scheduler;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        Executor executor = taskScheduler();
        taskRegistrar.setScheduler(executor);

        log.info("Scheduled tasks configured with executor: {}",
                executor.getClass().getSimpleName());
    }
}