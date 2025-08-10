package com.model.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "rocketmq")
public class RocketMqConfig {
    /**
     * RocketMQ服务端地址
     */
    private String mqAddr;
    /**
     * 消费组名称
     */
    private String consumerGroup;
    /**
     * 生产组名称
     */
    private String producerGroup;
}