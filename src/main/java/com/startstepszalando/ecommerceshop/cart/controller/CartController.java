package com.startstepszalando.ecommerceshop.cart.controller;

import com.startstepszalando.ecommerceshop.cart.model.Cart;
import com.startstepszalando.ecommerceshop.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> addProductToCart(@RequestParam Long productId, @RequestParam Integer quantity) {
        cartService.addProductToCart(productId, quantity);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/remove")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeProductFromCart(@RequestParam Long productId) {
        cartService.removeProductFromCart(productId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Cart> getMyCart() {
        Cart cart = cartService.getMyCart();
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/total")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BigDecimal> getCartTotal() {
        Long cartId = cartService.getMyCart().getId();
        BigDecimal totalCost = cartService.calculateTotalCost(cartId);
        return ResponseEntity.ok(totalCost);
    }
}