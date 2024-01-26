package com.startstepszalando.ecommerceshop.user.controller;

import com.google.gson.Gson;
import com.startstepszalando.ecommerceshop.exception.DuplicateUserException;
import com.startstepszalando.ecommerceshop.exception.TokenRefreshException;
import com.startstepszalando.ecommerceshop.jwt.JwtService;
import com.startstepszalando.ecommerceshop.refreshToken.dto.TokenRefreshRequest;
import com.startstepszalando.ecommerceshop.refreshToken.dto.TokenRefreshResponse;
import com.startstepszalando.ecommerceshop.refreshToken.model.RefreshToken;
import com.startstepszalando.ecommerceshop.refreshToken.service.RefreshTokenService;
import com.startstepszalando.ecommerceshop.user.dto.UserLoginRequest;
import com.startstepszalando.ecommerceshop.user.dto.UserRegistrationRequest;
import com.startstepszalando.ecommerceshop.user.model.Role;
import com.startstepszalando.ecommerceshop.user.model.User;
import com.startstepszalando.ecommerceshop.user.repository.UserRepository;
import com.startstepszalando.ecommerceshop.user.service.UserImpl;
import com.startstepszalando.ecommerceshop.user.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @MockBean
    private UserService userService;

    @MockBean
    private RefreshTokenService tokenService;

    @Test
    void givenValidLoginCredentials_ReturnAuthCookie() throws Exception {
        String email = "jdoe@example.com";
        User user = new User(21L, "John Doe", email, encoder.encode("lfjsler2342"), Role.ADMIN);

        UserLoginRequest credentials = new UserLoginRequest(email, "lfjsler2342");
        given(userService.loadUserByUsername("jdoe@example.com"))
                .willReturn(UserImpl.build(user));

        given(tokenService.createOrUpdateRefreshToken(anyLong())).willReturn(new RefreshToken());

        Gson gson = new Gson();
        ResultActions perform = mvc.perform(
                        post("/api/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(credentials))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.expiration").isNotEmpty())
                .andExpect(jsonPath("$.user").value("jdoe@example.com"));
    }

    @Test
    @Transactional
    void givenInvalidEmail_Return400Error() throws Exception {
        UserLoginRequest credentials = new UserLoginRequest("test", "password");
        Gson gson = new Gson();
        ResultActions perform = mvc.perform(
                        post("/api/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(credentials))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        String responseBody = perform.andReturn().getResponse().getContentAsString();
        assertThat(responseBody).contains("Please provide a valid email address");
    }

    @Test
    void givenInvalidPassword_Return401Error() throws Exception {
        User user = new User(5345L, "Test Person", "test37@gmail.com", encoder.encode("password123"), Role.CUSTOMER);
        UserLoginRequest credentials = new UserLoginRequest("test@gmail.com", "password");
        given(userService.loadUserByUsername("test@gmail.com"))
                .willReturn(UserImpl.build(user));
        Gson gson = new Gson();
        ResultActions perform = mvc.perform(
                        post("/api/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(credentials))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Bad credentials"));
    }


    @Test
    @Transactional
    void givenValidInput_RegistrationIsSuccessful() throws Exception {
        UserRegistrationRequest user = new UserRegistrationRequest("Test Person", "testing25@gmail.com", "password", Role.CUSTOMER);
        Gson gson = new Gson();

        MvcResult result = mvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(user))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        entityManager.flush();
        entityManager.clear();
        userRepository.deleteByEmail("testing25@gmail.com");
    }

    @Test
    @Transactional
    void givenShortPasswordLength_ReturnInvalidInputError() throws Exception {
        UserRegistrationRequest credentials = new UserRegistrationRequest("testing31@gmail.com", "testing31@gmail.com", "123", Role.CUSTOMER);
        Gson gson = new Gson();
        ResultActions perform = mvc.perform(
                        post("/api/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(credentials))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()) // Expect a 400 status code
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Invalid input: Password must be at least 8 characters long"));
        entityManager.flush();
        entityManager.clear();
        userRepository.deleteByEmail("testing31@gmail.com");
    }

    @Test
    @Transactional
    void givenDuplicateUserEmail_Return400Error() throws Exception, DuplicateUserException {
        UserRegistrationRequest credentials = new UserRegistrationRequest("Joey Doe", "jdoe@example.com", "password12345", Role.CUSTOMER);
        Gson gson = new Gson();

        given(userService.registerUser(any(UserRegistrationRequest.class)))
                .willThrow(new DuplicateUserException("Email is already in use"));

        ResultActions perform = mvc.perform(
                        post("/api/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(credentials))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()) // Expect a 400 status code
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Duplicate User Error: Email is already in use"));
    }

    @Test
    @Transactional
    void deleteUserByEmail() throws Exception {
        UserRegistrationRequest user = new UserRegistrationRequest("Test Person", "testing25@gmail.com", "password", Role.CUSTOMER);
        Gson gson = new Gson();
        mvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(user))
                .accept(MediaType.APPLICATION_JSON));
        userRepository.deleteByEmail("testing25@gmail.com");

        Optional<User> deletedUser = userRepository.findByEmail("testing25@gmail.com");
        assertFalse(deletedUser.isPresent(), "User should be deleted");
    }

    @Test
    void givenTokenIsNotInDB_ReturnError() throws Exception {
        TokenRefreshRequest tokenRequest = new TokenRefreshRequest();
        String token = UUID.randomUUID().toString();
        tokenRequest.setRefreshToken(token);

        given(tokenService.refreshToken(token, jwtService))
                .willThrow(new TokenRefreshException(token, "Refresh token is not in database!"));

        String expectedErrorMessage = String.format("Failed for [%s]: %s", token, "Refresh token is not in database!");
        Gson gson = new Gson();

        ResultActions perform = mvc.perform(
                        post("/api/users/refreshtoken")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(tokenRequest))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(expectedErrorMessage));
    }

    @Test
    void givenValidToken_ReturnAccessToken() throws Exception {
        User user = new User(6546L, "Tester G", "test22@gmail.com", encoder.encode("password"), Role.CUSTOMER);
        TokenRefreshRequest tokenRequest = new TokenRefreshRequest();
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setExpiryDate(Instant.now().plusMillis(15000));
        refreshToken.setUser(user);
        refreshToken.setToken(tokenValue);
        given(tokenService.findByToken(tokenValue)).willReturn(Optional.of(refreshToken));
        given(tokenService.verifyExpiration(refreshToken)).willReturn(refreshToken);
        given(tokenService.refreshToken(tokenValue, jwtService)).willReturn(new TokenRefreshResponse(tokenValue, tokenValue));

        tokenRequest.setRefreshToken(tokenValue);
        Gson gson = new Gson();
        ResultActions perform = mvc.perform(
                        post("/api/users/refreshtoken")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(tokenRequest))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void givenExpiredToken_ReturnError() throws Exception {
        User user = new User(6547L, "Tester C", "test11@gmail.com", "password", Role.CUSTOMER);
        TokenRefreshRequest tokenRequest = new TokenRefreshRequest();
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setExpiryDate(Instant.now().minusMillis(15000));
        refreshToken.setUser(user);
        refreshToken.setToken(tokenValue);
        tokenRequest.setRefreshToken(tokenValue);
        given(tokenService.findByToken(tokenValue)).willReturn(Optional.of(refreshToken));
        given(tokenService.refreshToken(tokenValue, jwtService)).willThrow(new TokenRefreshException(tokenValue, "Refresh token was expired. Please make a new signin request"));
        String expectedErrorMessage = String.format("Failed for [%s]: %s", tokenValue, "Refresh token was expired. Please make a new signin request");
        Gson gson = new Gson();
        ResultActions perform = mvc.perform(
                        post("/api/users/refreshtoken")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(tokenRequest))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(expectedErrorMessage));
    }
}