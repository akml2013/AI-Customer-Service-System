package com.qa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ThreadPoolConfig {

    @Bean("modelTaskExecutor")
    public Executor modelTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数 (CPU核心数 * 2)
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors() * 2);
        // 最大线程数
        executor.setMaxPoolSize(50);
        // 队列容量
        executor.setQueueCapacity(100);
        // 线程名前缀
        executor.setThreadNamePrefix("model-task-");
        // 拒绝策略 (调用者运行)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 线程空闲时间
        executor.setKeepAliveSeconds(60);
        // 初始化
        executor.initialize();
        return executor;
    }
}
