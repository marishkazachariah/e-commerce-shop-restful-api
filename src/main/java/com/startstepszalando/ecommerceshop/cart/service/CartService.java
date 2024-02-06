package com.startstepszalando.ecommerceshop.cart.service;

import com.startstepszalando.ecommerceshop.cart.model.Cart;
import com.startstepszalando.ecommerceshop.cart.model.CartItem;
import com.startstepszalando.ecommerceshop.cart.model.CartItemId;
import com.startstepszalando.ecommerceshop.cart.repository.CartItemRepository;
import com.startstepszalando.ecommerceshop.cart.repository.CartRepository;
import com.startstepszalando.ecommerceshop.product.model.Product;
import com.startstepszalando.ecommerceshop.product.repository.ProductRepository;
import com.startstepszalando.ecommerceshop.user.model.User;
import com.startstepszalando.ecommerceshop.user.repository.UserRepository;
import com.startstepszalando.ecommerceshop.user.service.UserImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    public Cart createCartForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    public Cart getMyCart() {
        Long userId = getCurrentUserId();
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for userId: " + userId));
    }

    @Transactional
    public void addProductToCart(Long productId, Integer quantity) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCartForUser(userId));

        CartItem cartItem = cartItemRepository.findById(new CartItemId(cart.getId(), productId))
                .orElse(new CartItem(cart, product, 0));
        cartItem.setQuantity(cartItem.getQuantity() + quantity);

        cartItemRepository.save(cartItem);
    }

    @Transactional
    public void removeProductFromCart(Long productId) {
        Long userId = getCurrentUserId();
        Cart cart = getMyCart(); // Use getMyCart to fetch the cart
        CartItemId cartItemId = new CartItemId(cart.getId(), productId);
        cartItemRepository.deleteById(cartItemId);
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserImpl) { // Ensure it's an instance of UserImpl
            return ((UserImpl) principal).getId();
        } else {
            throw new RuntimeException("Expected principal to be an instance of UserImpl");
        }
    }

    public BigDecimal calculateTotalCost(Long cartId) {
        Optional<Cart> cart = cartRepository.findById(cartId);
        return cart.map(c -> c.getItems().stream()
                        .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .orElse(BigDecimal.ZERO);
    }
}