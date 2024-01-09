package com.startstepszalando.ecommerceshop.user.dto;

import com.startstepszalando.ecommerceshop.user.model.Role;
import lombok.Data;

import java.util.Set;

@Data
public class UserRegistrationDto {
    private String name;
    private String email;
    private String password;
    private Set<Role> roles;
}

