package com.startstepszalando.ecommerceshop.user.service;

import com.startstepszalando.ecommerceshop.auth.AuthenticationResponse;
import com.startstepszalando.ecommerceshop.exception.DuplicateUserException;
import com.startstepszalando.ecommerceshop.exception.UserNotFoundException;
import com.startstepszalando.ecommerceshop.jwt.JwtService;
import com.startstepszalando.ecommerceshop.user.dto.UserRegistrationRequest;
import com.startstepszalando.ecommerceshop.user.model.User;
import com.startstepszalando.ecommerceshop.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;

@Service
@Primary
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public User getUserByEmail(String email) throws UserNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    public AuthenticationResponse registerUser(UserRegistrationRequest request) throws DuplicateUserException {
        try {
            User user = User.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(request.getRole())
                    .build();
            userRepository.save(user);

            logger.info("Registration successful with email: {}", request.getEmail());
            var jwtToken = jwtService.generateToken(user);

            return AuthenticationResponse.builder()
                    .jwtToken(jwtToken)
                    .message("User registered successfully")
                    .build();
        } catch (DataIntegrityViolationException e) {
            if (Objects.requireNonNull(e.getRootCause()).getMessage().contains("Duplicate entry")) {
                throw new DuplicateUserException("Duplicate User Error: Email is already in use");
            }
            throw e;
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UserNotFoundException {
        User user = getUserByEmail(username);
        logger.info("User logged in with email: {}", username);

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities);
    }
}
