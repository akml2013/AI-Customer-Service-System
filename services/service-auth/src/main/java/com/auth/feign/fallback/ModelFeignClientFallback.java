package com.auth.feign.fallback;

import com.auth.feign.ModelFeignClient;
import org.springframework.stereotype.Component;

@Component
public class ModelFeignClientFallback implements ModelFeignClient {

    @Override
    public void cleanupUserConnections(String clientId) {
        System.out.println("ModelFeignClientFallback 兜底回调");
    }
}
