package com.startstepszalando.ecommerceshop.product.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProductRequest {
    @NotNull(message = "Product name cannot be null.")
    @NotEmpty(message = "Product name cannot be empty.")
    private String name;

    @DecimalMin(value = "0", message = "Price must be a positive number.")
    private float price;

    @NotNull(message = "Product description cannot be null.")
    @NotEmpty(message = "Product description cannot be empty.")
    private String description;

    @NotNull(message = "Product price cannot be null.")
    @Min(value = 0, message = "Stock quantity must be a positive number.")
    private int stock;
}