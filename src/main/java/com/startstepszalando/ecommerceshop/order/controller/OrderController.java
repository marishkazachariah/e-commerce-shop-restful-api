package com.startstepszalando.ecommerceshop.order.controller;

import com.startstepszalando.ecommerceshop.cart.model.Cart;
import com.startstepszalando.ecommerceshop.cart.service.CartService;
import com.startstepszalando.ecommerceshop.exception.product.InsufficientStockException;
import com.startstepszalando.ecommerceshop.exception.product.ProductNotFoundException;
import com.startstepszalando.ecommerceshop.order.dto.OrderResponse;
import com.startstepszalando.ecommerceshop.order.model.Order;
import com.startstepszalando.ecommerceshop.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    private final CartService cartService;

    public OrderController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @PostMapping("/createOrderFromCart")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createOrderFromCart() throws InsufficientStockException, ProductNotFoundException, AccessDeniedException {
        Cart cart = cartService.getMyCart();
        Order order = orderService.createOrderFromCart(cart);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        OrderResponse orderResponse = orderService.getOrderDTO(order.getId(), currentUsername);

        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId, Authentication authentication) throws AccessDeniedException {
        String currentUsername = authentication.getName();
        OrderResponse orderResponse = orderService.getOrderDTO(orderId, currentUsername);
        return ResponseEntity.ok(orderResponse);
    }
}