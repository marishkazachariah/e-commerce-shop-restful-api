package com.startstepszalando.ecommerceshop.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    private String name;
    private BigDecimal price;
    private String description;
    private Integer stock;
    private Long adminId;
}
