package com.startstepszalando.ecommerceshop.user.repository;

import com.startstepszalando.ecommerceshop.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    void deleteByEmail(String mail);
}
