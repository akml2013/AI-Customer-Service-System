package com.model.controller;
import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@RestController
@RequestMapping("/sse")
public class MonitorController {

    // 添加计数器获取方法
    private final AtomicLong processedCount = new AtomicLong(0);
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong redisCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);

    @GetMapping("/monitor/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("processed", processedCount.get());
        stats.put("success", successCount.get());
        stats.put("redisCount", redisCount.get());
        stats.put("error", errorCount.get());
        return stats;
    }

    @GetMapping("/monitor/reset")
    public String resetStats() {
        processedCount.set(0);
        successCount.set(0);
        redisCount.set(0);
        errorCount.set(0);
        return "Stats reset";
    }

}
