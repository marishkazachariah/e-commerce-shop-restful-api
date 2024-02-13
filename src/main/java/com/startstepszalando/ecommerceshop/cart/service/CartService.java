package com.startstepszalando.ecommerceshop.cart.service;

import com.startstepszalando.ecommerceshop.cart.dto.CartItemResponse;
import com.startstepszalando.ecommerceshop.cart.dto.CartResponse;
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
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(UserRepository userRepository, CartRepository cartRepository,
                       CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    public Cart getMyCart() {
        Long userId = getCurrentUserId();

        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createCartForUser(userId));
    }

    public CartResponse getMyCartDetails() {
        Long userId = getCurrentUserId();
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCartForUser(userId));

        List<CartItemResponse> cartItems = cartItemRepository.findCartDetailsByUserId(userId)
                .stream()
                .map(item -> new CartItemResponse(item.getProductId(), item.getProductName(), item.getQuantity(), item.getPrice()))
                .toList();

        String message = cartItems.isEmpty() ? "The user's cart is empty" : "";

        return new CartResponse(cart.getId(), cartItems, message);
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

    @Transactional
    public Cart saveCart(Cart cart) {
        return cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(Cart cart) {
        cartItemRepository.deleteByCartId(cart.getId());

        cart.getItems().clear();
        saveCart(cart);
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
        CartResponse cartResponse = getMyCartDetails();

        List<CartItemResponse> cartItems = cartResponse.getItems();

        return cartItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}