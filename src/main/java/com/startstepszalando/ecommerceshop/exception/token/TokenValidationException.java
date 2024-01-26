package com.startstepszalando.ecommerceshop.exception.token;

public class TokenValidationException extends RuntimeException {
    public TokenValidationException(String message) {
        super(message);
    }
}
