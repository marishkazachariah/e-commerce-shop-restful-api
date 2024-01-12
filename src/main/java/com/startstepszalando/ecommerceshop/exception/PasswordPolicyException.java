package com.startstepszalando.ecommerceshop.exception;

public class PasswordPolicyException extends RuntimeException {
    public PasswordPolicyException() {
        super("Password does not meet the security criteria");
    }
}
