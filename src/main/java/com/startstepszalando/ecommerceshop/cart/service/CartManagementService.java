package com.startstepszalando.ecommerceshop.cart.service;

import com.startstepszalando.ecommerceshop.cart.model.Cart;
import com.startstepszalando.ecommerceshop.cart.repository.CartRepository;
import com.startstepszalando.ecommerceshop.user.model.User;
import com.startstepszalando.ecommerceshop.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class CartManagementService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    public CartManagementService(CartRepository cartRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
    }

    public Cart createCartForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Optional<Cart> existingCart = cartRepository.findByUserId(userId);
        if (existingCart.isPresent()) {
            throw new IllegalStateException("User already has a cart");
        }

        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }
}
