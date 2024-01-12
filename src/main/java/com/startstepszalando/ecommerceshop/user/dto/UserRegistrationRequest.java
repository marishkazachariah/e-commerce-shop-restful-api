package com.startstepszalando.ecommerceshop.user.dto;

import com.startstepszalando.ecommerceshop.user.model.Role;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationRequest {
    private String name;
    private String email;
    private String password;
    private Set<Role> roles;
}

