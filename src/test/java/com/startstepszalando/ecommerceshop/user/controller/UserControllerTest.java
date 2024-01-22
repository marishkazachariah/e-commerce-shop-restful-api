package com.startstepszalando.ecommerceshop.user.controller;

import com.google.gson.Gson;
import com.startstepszalando.ecommerceshop.jwt.JwtService;
import com.startstepszalando.ecommerceshop.user.dto.UserLoginRequest;
import com.startstepszalando.ecommerceshop.user.dto.UserRegistrationRequest;
import com.startstepszalando.ecommerceshop.user.model.Role;
import com.startstepszalando.ecommerceshop.user.model.User;
import com.startstepszalando.ecommerceshop.user.repository.UserRepository;
import com.startstepszalando.ecommerceshop.user.service.UserService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
    private UserService userService;

    @Test
    @Transactional
    void givenValidCredentials_ReturnAuthCookie() throws Exception {
        User user = new User(645L, "test", "testing2@gmail.com", encoder.encode("password"), Role.CUSTOMER);
        userRepository.save(user);
        UserLoginRequest credentials = new UserLoginRequest("testing2@gmail.com", "password");
        Gson gson = new Gson();

        mvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(credentials))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("User logged in successfully"));

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
    @Transactional
    void givenInvalidCredentials_Return401Error() throws Exception {
        UserLoginRequest credentials = new UserLoginRequest("testing333@gmail.com", "passwords");
        Gson gson = new Gson();
        ResultActions perform = mvc.perform(
                        post("/api/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(credentials))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        String responseBody = perform.andReturn().getResponse().getContentAsString();
        assertThat(responseBody).contains("Invalid username and/or password");
    }

    @Test
    @Transactional
    void givenValidInput_RegistrationIsSuccessful() throws Exception {
        UserRegistrationRequest user = new UserRegistrationRequest("test", "testing23@gmail.com", "password", Role.CUSTOMER);
        Gson gson = new Gson();
        mvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(user))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        userRepository.deleteByEmail("testing23@gmail.com");
    }

    @Test
    @Transactional
    void givenDuplicateUser_ThrowsDuplicateUserException() throws Exception {
        UserRegistrationRequest secondUser = new UserRegistrationRequest("Joe Doe", "jdoe@example.com", "password2", Role.CUSTOMER);
        Gson gson = new Gson();
        ResultActions resultActions = mvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(secondUser))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo("Duplicate user with the same email already exists");
    }

    @AfterEach
    @BeforeEach
    void cleanup() {
        userRepository.deleteByEmail("testing2@gmail.com");
        userRepository.deleteByEmail("testing23@gmail.com");
    }
}