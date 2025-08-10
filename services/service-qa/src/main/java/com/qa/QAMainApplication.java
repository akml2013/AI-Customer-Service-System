package com.qa;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@EnableFeignClients //开启Feign远程调用功能
@EnableDiscoveryClient //开启服务发现
@SpringBootApplication
@MapperScan("com.qa.dao")
public class QAMainApplication {
    public static void main(String[] args) {

        SpringApplication.run(QAMainApplication.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(NacosConfigManager nacosConfigManager){
        return new ApplicationRunner() {
            @Override
            public void run(ApplicationArguments args) throws Exception {
                ConfigService configService = nacosConfigManager.getConfigService();
                configService.addListener("service-order.properties", "DEFAULT_GROUP", new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return Executors.newFixedThreadPool(4);
                    }

                    @Override
                    public void receiveConfigInfo(String s) {
                        System.out.println(s);
                    }
                });
            }
        };
    };
}
