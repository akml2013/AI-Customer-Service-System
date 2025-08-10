package com.product.bean;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
public class Product {
    private Long id;
    private String name;
    private BigDecimal price;
    private int num;

}
