package com.startstepszalando.ecommerceshop.user.repository;

import com.startstepszalando.ecommerceshop.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
