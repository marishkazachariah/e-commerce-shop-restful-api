package com.startstepszalando.ecommerceshop.exception.product;

import com.startstepszalando.ecommerceshop.product.model.Product;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class InsufficientStockException extends Exception {
    private final Set<Product> unavailableProducts = new HashSet<>();
    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String message, Product product) {
        super(message);
        unavailableProducts.add(product);
    }
}
