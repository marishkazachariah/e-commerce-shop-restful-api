package com.startstepszalando.ecommerceshop.product.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductPaginationRequest {
    private List<ProductRequest> products;
    private int currentPage;
    private long offset;
    private int limit;
    private int totalPages;
    private long totalProducts;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class ProductRequest {
        @NotNull(message = "Product name cannot be null.")
        @NotEmpty(message = "Product name cannot be empty.")
        private String name;

        @DecimalMin(value = "0", message = "Price must be a positive number.")
        private BigDecimal price;

        @NotNull(message = "Product description cannot be null.")
        @NotEmpty(message = "Product description cannot be empty.")
        private String description;

        @NotNull(message = "Product price cannot be null.")
        @Min(value = 0, message = "Stock quantity must be a positive number.")
        private int stock;
    }
}
