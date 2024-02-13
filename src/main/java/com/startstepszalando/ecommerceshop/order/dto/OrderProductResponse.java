package com.startstepszalando.ecommerceshop.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderProductResponse {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
}

