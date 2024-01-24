package com.startstepszalando.ecommerceshop.refreshToken.dto;

import lombok.Data;

@Data
public class TokenRefreshRequest {
    private String refreshToken;
}