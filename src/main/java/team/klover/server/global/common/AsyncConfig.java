package team.klover.server.global.common;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public @NotNull Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // 기본 스레드 수 설정
        executor.setMaxPoolSize(10); // 최대 스레드 수 설정
        executor.setQueueCapacity(50); // 큐 용량 설정
        executor.setThreadNamePrefix("AsyncScheduler-"); // 스레드 이름 접두사 설정
        executor.initialize(); // 스레드 풀 초기화
        return executor;
    }
}
