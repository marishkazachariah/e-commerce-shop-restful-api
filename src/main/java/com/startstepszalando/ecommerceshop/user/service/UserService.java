package com.startstepszalando.ecommerceshop.user.service;

import com.startstepszalando.ecommerceshop.auth.AuthenticationResponse;
import com.startstepszalando.ecommerceshop.exception.DuplicateUserException;
import com.startstepszalando.ecommerceshop.exception.RegistrationException;
import com.startstepszalando.ecommerceshop.exception.UserNotFoundException;
import com.startstepszalando.ecommerceshop.jwt.JwtService;
import com.startstepszalando.ecommerceshop.user.dto.UserRegistrationRequest;
import com.startstepszalando.ecommerceshop.user.model.User;
import com.startstepszalando.ecommerceshop.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
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

    public boolean isEmailRegistered(String email)  {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        return optionalUser.isPresent();
    }

    public AuthenticationResponse registerUser(UserRegistrationRequest request) throws DuplicateUserException {
        logger.info("Registering user with email: {}", request.getEmail());

        try {
            if (isEmailRegistered(request.getEmail())) {
                logger.error("Registration failed: Duplicate user with email {}", request.getEmail());
                throw new DuplicateUserException("Email already in use");
            }

            User user = User.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .roles(request.getRoles())
                    .build();
            userRepository.save(user);
            logger.info("Registration successful with email: {}", request.getEmail());
            var jwtToken = jwtService.generateToken(user);

            return AuthenticationResponse.builder()
                    .jwt(jwtToken)
                    .build();

        } catch (DuplicateUserException e) {
            logger.error("Registration failed: Duplicate user with email {}", request.getEmail());
            throw e;
        } catch (Exception e) {
            logger.error("Registration failed: Unexpected error for user {}", request.getEmail(), e);
            throw new RegistrationException("Unexpected error occurred during registration");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UserNotFoundException {
        try {
            User user = getUserByEmail(username);

            logger.info("User logged in with email: {}", username);

            Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    authorities);
        } catch (UserNotFoundException e) {
            logger.error("User not found: {}", username);
            throw e;
        }
    }

}
