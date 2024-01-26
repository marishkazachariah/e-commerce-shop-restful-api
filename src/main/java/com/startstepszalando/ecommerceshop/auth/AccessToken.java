package com.startstepszalando.ecommerceshop.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AccessToken {
    private String user;
    private List<String> roles;
    private String accessToken;
    private String refreshToken;
    private String expiration;
}