package com.startstepszalando.ecommerceshop.cart.service;

import com.startstepszalando.ecommerceshop.cart.dto.CartItemResponse;
import com.startstepszalando.ecommerceshop.cart.dto.CartResponse;
import com.startstepszalando.ecommerceshop.cart.model.Cart;
import com.startstepszalando.ecommerceshop.cart.model.CartItem;
import com.startstepszalando.ecommerceshop.cart.model.CartItemId;
import com.startstepszalando.ecommerceshop.cart.repository.CartItemRepository;
import com.startstepszalando.ecommerceshop.cart.repository.CartRepository;
import com.startstepszalando.ecommerceshop.exception.product.ProductNotFoundException;
import com.startstepszalando.ecommerceshop.product.model.Product;
import com.startstepszalando.ecommerceshop.product.repository.ProductRepository;
import com.startstepszalando.ecommerceshop.user.service.UserImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CartManagementService cartManagementService;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       CartManagementService cartManagementService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.cartManagementService = cartManagementService;
    }

    public Cart getMyCart() {
        Long userId = getCurrentUserId();

        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartManagementService.createCartForUser(userId));
    }

    public CartResponse getMyCartDetails() {
        Long userId = getCurrentUserId();
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> cartManagementService.createCartForUser(userId));

        List<CartItemResponse> cartItems = cartItemRepository.findCartDetailsByUserId(userId)
                .stream()
                .map(item -> new CartItemResponse(item.getProductId(), item.getProductName(), item.getQuantity(), item.getPrice()))
                .toList();

        String message = cartItems.isEmpty() ? "The user's cart is empty" : "";

        return new CartResponse(cart.getId(), cartItems, message);
    }

    @Transactional
    public void addProductToCart(Long productId, Integer quantity) throws ProductNotFoundException {
        Long userId = getCurrentUserId();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("Insufficient stock for product");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> cartManagementService.createCartForUser(userId));

        Optional<CartItem> existingCartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        CartItem cartItem;
        if (existingCartItem.isPresent()) {
            cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cart.getItems().add(cartItem);
        }

        cartItemRepository.save(cartItem);
        cartRepository.save(cart);
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

    public BigDecimal calculateTotalCost() {
        CartResponse cartResponse = getMyCartDetails();

        List<CartItemResponse> cartItems = cartResponse.getItems();

        return cartItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}