package com.startstepszalando.ecommerceshop.refreshToken.service;

import com.startstepszalando.ecommerceshop.exception.TokenRefreshException;
import com.startstepszalando.ecommerceshop.exception.UserNotFoundException;
import com.startstepszalando.ecommerceshop.refreshToken.model.RefreshToken;
import com.startstepszalando.ecommerceshop.refreshToken.repository.RefreshTokenRepository;
import com.startstepszalando.ecommerceshop.user.model.User;
import com.startstepszalando.ecommerceshop.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${app.jwt.expiration-ms}")
    private Long refreshTokenDurationMs;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public Optional<RefreshToken> findByToken(String token) {
        System.out.println("find token is: " + token);
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createOrUpdateRefreshToken(Long userId) {
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserId(userId); // Note this change
        RefreshToken refreshToken;

        if (existingToken.isPresent()) {
            // Update the existing token
            refreshToken = existingToken.get();
            refreshToken.setToken(UUID.randomUUID().toString()); // Generating a new token
            refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs)); // Setting new expiry date
        } else {
            // Create a new token
            refreshToken = new RefreshToken();
            refreshToken.setUser(userRepository.findById(userId).orElseThrow(() ->
                    new UserNotFoundException("User not found with id: " + userId))); // Set the user
            refreshToken.setToken(UUID.randomUUID().toString()); // Generate token
            refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs)); // Set expiry date
        }

        return refreshTokenRepository.save(refreshToken);
    }


    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please login again.");
        }
        return token;
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            return refreshTokenRepository.deleteByUser(userOptional.get());
        } else {
            throw new UserNotFoundException("User not found with id: " + userId);
        }
    }
}
