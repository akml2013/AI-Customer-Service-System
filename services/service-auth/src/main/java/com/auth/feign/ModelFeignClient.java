package com.auth.feign;

import com.auth.feign.fallback.ModelFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "service-model", fallback = ModelFeignClientFallback.class)
public interface ModelFeignClient {
    @GetMapping("/sse/disconnectById")
    void cleanupUserConnections(@RequestParam String clientId);
}
