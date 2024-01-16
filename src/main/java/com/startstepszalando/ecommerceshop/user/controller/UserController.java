package com.startstepszalando.ecommerceshop.user.controller;

import com.startstepszalando.ecommerceshop.auth.AuthenticationResponse;
import com.startstepszalando.ecommerceshop.exception.DuplicateUserException;
import com.startstepszalando.ecommerceshop.exception.InvalidCredentialsException;
import com.startstepszalando.ecommerceshop.exception.RegistrationException;
import com.startstepszalando.ecommerceshop.exception.UserNotFoundException;
import com.startstepszalando.ecommerceshop.jwt.JwtService;
import com.startstepszalando.ecommerceshop.user.dto.UserLoginRequest;
import com.startstepszalando.ecommerceshop.user.dto.UserRegistrationRequest;
import com.startstepszalando.ecommerceshop.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Operation(summary = "Register a new user", responses = {
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid user details or validation error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RegistrationException.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or token",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = InvalidCredentialsException.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserNotFoundException.class))),
            @ApiResponse(responseCode = "409", description = "Duplicate user detected",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DuplicateUserException.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error or unexpected error during registration",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Exception.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> registerUser(
            @RequestBody UserRegistrationRequest request
    ) throws DuplicateUserException {
        return ResponseEntity.ok(userService.registerUser(request));
    }

    @PostMapping(value = "/login")
    public ResponseEntity<?> login(@Validated @RequestBody UserLoginRequest loginRequest){
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            ));

            UserDetails userDetails = userService.loadUserByUsername(loginRequest.getEmail());

            final String jwt = jwtService.generateToken(userDetails);

            return ResponseEntity.ok(new AuthenticationResponse(jwt));
        } catch (AuthenticationException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username and/or password.");
        }
    }
}
