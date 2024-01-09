package com.startstepszalando.ecommerceshop.user.service;

import com.startstepszalando.ecommerceshop.user.dto.UserDto;
import com.startstepszalando.ecommerceshop.user.model.User;
import com.startstepszalando.ecommerceshop.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void registerUser(UserDto userDto) {
        User user = new User();
        User.builder().name(userDto.getName());
        User.builder().email(userDto.getEmail());
        User.builder().password(passwordEncoder.encode(userDto.getPassword()));
        User.builder().roles(userDto.getRoles());
        userRepository.save(user);
    }
}
