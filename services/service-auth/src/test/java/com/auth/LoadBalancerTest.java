package com.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

@SpringBootTest
public class LoadBalancerTest {
    @Autowired
    LoadBalancerClient loadBalancerClient;
    DiscoveryClient discoveryClient;

    @Test
    void loadBalancerClient() {
        ServiceInstance instance = loadBalancerClient.choose("service-product");
        System.out.println("choose: " + instance.getHost() + ":" + instance.getPort());
        instance = loadBalancerClient.choose("service-product");
        System.out.println("choose: " + instance.getHost() + ":" + instance.getPort());
        instance = loadBalancerClient.choose("service-product");
        System.out.println("choose: " + instance.getHost() + ":" + instance.getPort());
        instance = loadBalancerClient.choose("service-product");
        System.out.println("choose: " + instance.getHost() + ":" + instance.getPort());
        instance = loadBalancerClient.choose("service-product");
        System.out.println("choose: " + instance.getHost() + ":" + instance.getPort());
    }
}
