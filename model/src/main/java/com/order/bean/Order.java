package com.order.bean;

import com.product.bean.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Order {
    private Long id;
    private BigDecimal totalAmount;
    private Long userId;
    private String nickname;
    private String address;
    private List<Product> productList;
}
