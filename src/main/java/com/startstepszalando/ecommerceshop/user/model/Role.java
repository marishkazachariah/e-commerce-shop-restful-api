package com.startstepszalando.ecommerceshop.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    CUSTOMER("Customer"),
    ADMIN("Admin");

    private final String ROLE;
}
