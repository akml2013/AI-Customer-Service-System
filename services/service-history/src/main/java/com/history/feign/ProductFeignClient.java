package com.history.feign;

import com.history.feign.fallback.ProductFeignClientFallback;
import com.product.bean.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(value = "service-product", fallback = ProductFeignClientFallback.class) //Feign客户端
public interface ProductFeignClient {

    //1、标注在Controller上，是接收这样的请求
    //2、标注在FeignClient上，是发送这样的请求
    @GetMapping("/product/{id}")
    Product getProductById(@PathVariable("id") Long productId);

    @PutMapping("/product/update/{id}")
    void updateProduct(@PathVariable("id") Long productId);
}
