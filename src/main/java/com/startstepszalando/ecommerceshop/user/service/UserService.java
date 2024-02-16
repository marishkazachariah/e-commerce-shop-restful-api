package com.startstepszalando.ecommerceshop.user.service;

import com.startstepszalando.ecommerceshop.auth.AuthenticationResponse;
import com.startstepszalando.ecommerceshop.exception.user.DuplicateUserException;
import com.startstepszalando.ecommerceshop.exception.user.UserNotFoundException;
import com.startstepszalando.ecommerceshop.jwt.JwtService;
import com.startstepszalando.ecommerceshop.user.dto.UserRegistrationRequest;
import com.startstepszalando.ecommerceshop.user.model.User;
import com.startstepszalando.ecommerceshop.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Primary
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public AuthenticationResponse registerUser(UserRegistrationRequest request) throws DuplicateUserException {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("Email is already in use");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        userRepository.save(user);
        logger.info("Registration successful with email: {}", request.getEmail());

        UserDetails userDetails = UserImpl.build(user);
        String jwtToken = jwtService.generateToken(userDetails);

        return AuthenticationResponse.builder()
                .jwtToken(jwtToken)
                .message("User registered successfully")
                .build();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UserNotFoundException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email must not be empty");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        return UserImpl.build(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public boolean isValidUserDetails(User user) {
        return user.getEmail() != null && !user.getEmail().trim().isEmpty();
    }

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ADMIN"));
    }
}
