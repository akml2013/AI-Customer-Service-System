package com.history.feign.fallback;

import com.history.feign.ProductFeignClient;
import com.product.bean.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProductFeignClientFallback implements ProductFeignClient {
    @Override
    public Product getProductById(Long productId) {
        System.out.println("ProductFeignClientFallback 兜底回调");
        Product product = new Product();
        product.setId(productId);
        product.setName("fallback");
        product.setPrice(new BigDecimal("100.00"));
        return product;
    }

    @Override
    public void updateProduct(Long productId) {
        System.out.println("ProductFeignClientFallback 兜底回调");
    }
}
