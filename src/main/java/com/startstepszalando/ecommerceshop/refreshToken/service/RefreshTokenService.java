package com.startstepszalando.ecommerceshop.refreshToken.service;

import com.startstepszalando.ecommerceshop.exception.token.TokenRefreshException;
import com.startstepszalando.ecommerceshop.exception.user.UserNotFoundException;
import com.startstepszalando.ecommerceshop.jwt.JwtService;
import com.startstepszalando.ecommerceshop.refreshToken.dto.TokenRefreshResponse;
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
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createOrUpdateRefreshToken(Long userId) {
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserId(userId);
        RefreshToken refreshToken;
        if (existingToken.isPresent()) {
            // Update the existing token
            refreshToken = existingToken.get();
            refreshToken.setToken(UUID.randomUUID().toString());
            refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        } else {
            // Create a new token
            refreshToken = new RefreshToken();
            refreshToken.setUser(userRepository.findById(userId).orElseThrow(() ->
                    new UserNotFoundException("User not found with id: " + userId)));
            refreshToken.setToken(UUID.randomUUID().toString());
            refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
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

    public TokenRefreshResponse refreshToken(String requestRefreshToken, JwtService jwtService) throws TokenRefreshException {
        RefreshToken refreshToken = findByToken(requestRefreshToken)
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token is not in database!"));

        verifyExpiration(refreshToken);
        User user = refreshToken.getUser();
        String token = jwtService.generateTokenFromUsername(user.getEmail());
        return new TokenRefreshResponse(token, requestRefreshToken);
    }
}
