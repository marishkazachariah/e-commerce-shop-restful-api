package com.startstepszalando.ecommerceshop.user.controller;

import com.startstepszalando.ecommerceshop.auth.AccessToken;
import com.startstepszalando.ecommerceshop.auth.AuthenticationResponse;
import com.startstepszalando.ecommerceshop.exception.*;
import com.startstepszalando.ecommerceshop.jwt.JwtService;
import com.startstepszalando.ecommerceshop.refreshToken.dto.TokenRefreshRequest;
import com.startstepszalando.ecommerceshop.refreshToken.dto.TokenRefreshResponse;
import com.startstepszalando.ecommerceshop.refreshToken.model.RefreshToken;
import com.startstepszalando.ecommerceshop.refreshToken.service.RefreshTokenService;
import com.startstepszalando.ecommerceshop.user.dto.UserLoginRequest;
import com.startstepszalando.ecommerceshop.user.dto.UserRegistrationRequest;
import com.startstepszalando.ecommerceshop.user.repository.UserRepository;
import com.startstepszalando.ecommerceshop.user.service.UserImpl;
import com.startstepszalando.ecommerceshop.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @Operation(summary = "Register a new user", responses = {
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserNotFoundException.class))),
            @ApiResponse(responseCode = "400", description = "Duplicate user detected",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DuplicateUserException.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error or unexpected error during registration",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Exception.class)))
    })

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Valid
            @RequestBody UserRegistrationRequest request
    ) {
        try {
            AuthenticationResponse response = userService.registerUser(request);
            return ResponseEntity.ok(response);
        } catch (DuplicateUserException e) {
            ErrorMessage errorMessage = new ErrorMessage(
                    400, new Date(),
                    "Duplicate User Error: Email is already in use",
                    "");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(errorMessage);
        }
    }

    @Operation(summary = "User login", responses = {
            @ApiResponse(responseCode = "200", description = "User logged in successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserNotFoundException.class))),
            @ApiResponse(responseCode = "401", description = "Bad credentials",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthenticationException.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Exception.class)))
    })
    @PostMapping(value = "/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequest loginRequest) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserImpl userDetails = (UserImpl) authentication.getPrincipal();

        ResponseCookie jwtCookie = jwtService.generateJwtCookie(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        RefreshToken refreshToken = refreshTokenService.createOrUpdateRefreshToken(userDetails.getId());
        AccessToken responseBody = new AccessToken(userDetails.getUsername(), roles, jwtCookie.getValue(), refreshToken.getToken(), jwtCookie.getMaxAge().toString());

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(responseBody);
    }

    @Operation(summary = "Refresh authentication token: Provide a valid refresh token to receive a new authentication token", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully refreshed authentication token",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TokenRefreshResponse.class))),
            @ApiResponse(responseCode = "404", description = "Refresh token is not in database",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TokenValidationException.class))),
            @ApiResponse(responseCode = "403", description = "Refresh token was expired",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TokenRefreshException.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Exception.class)))
    })
    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtService.generateTokenFromUsername(user.getEmail());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }
}
