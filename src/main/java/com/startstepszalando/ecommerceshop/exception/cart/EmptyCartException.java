package com.startstepszalando.ecommerceshop.exception.cart;

public class EmptyCartException extends RuntimeException {
    public EmptyCartException(String message) {
        super(message);
    }
}

