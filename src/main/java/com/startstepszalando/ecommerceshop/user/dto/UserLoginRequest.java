package com.startstepszalando.ecommerceshop.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class UserLoginRequest {
    @Email(message = "Please provide a valid email address")
    @NotNull (message = "Email must not be null")
    @NotEmpty(message = "Email must not be empty")
    private String email;

    @NotNull (message = "Password must not be null")
    @NotEmpty(message = "Password must not be empty")
    private String password;
}