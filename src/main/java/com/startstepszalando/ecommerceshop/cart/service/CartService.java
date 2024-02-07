package com.startstepszalando.ecommerceshop.cart.service;

import com.startstepszalando.ecommerceshop.cart.dto.CartItemRequest;
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
import java.util.List;

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

    public Cart getMyCart() {
        Long userId = getCurrentUserId();
        System.out.println("user id is: " + userId);
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for userId: " + userId));
    }

    public List<CartItemRequest> getMyCartDetails() {
        Long userId = getCurrentUserId();

        List<CartItemRequest> cartItems = cartItemRepository.findCartDetailsByUserId(userId);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart not found for userId: " + userId);
        }

        return cartItems;
    }

    @Transactional
    public void addProductToCart(Long productId, Integer quantity) {
        Long userId = getCurrentUserId();

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
        Cart cart = getMyCart();
        CartItemId cartItemId = new CartItemId(cart.getId(), productId);
        cartItemRepository.deleteById(cartItemId);
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserImpl) {
            return ((UserImpl) principal).getId();
        } else {
            throw new RuntimeException("Expected principal to be an instance of UserImpl");
        }
    }

    public Cart createCartForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    public BigDecimal calculateTotalCost() {
        List<CartItemRequest> cartItems = getMyCartDetails();

        BigDecimal totalCost = cartItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalCost;
    }
}