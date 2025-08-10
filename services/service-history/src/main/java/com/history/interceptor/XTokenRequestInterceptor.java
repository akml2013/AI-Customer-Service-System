package com.history.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class XTokenRequestInterceptor implements RequestInterceptor {

    /**
     * 请求拦截器
     * @param requestTemplate 请求模板
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("X-Token", UUID.randomUUID().toString());
    }
}
