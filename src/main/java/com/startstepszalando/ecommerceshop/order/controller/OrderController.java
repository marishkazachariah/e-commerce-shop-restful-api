package com.startstepszalando.ecommerceshop.order.controller;

import com.startstepszalando.ecommerceshop.cart.model.Cart;
import com.startstepszalando.ecommerceshop.cart.service.CartService;
import com.startstepszalando.ecommerceshop.exception.product.InsufficientStockException;
import com.startstepszalando.ecommerceshop.exception.product.ProductNotFoundException;
import com.startstepszalando.ecommerceshop.order.dto.OrderResponse;
import com.startstepszalando.ecommerceshop.order.model.Order;
import com.startstepszalando.ecommerceshop.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @PostMapping("/createOrderFromCart")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createOrderFromCart() throws InsufficientStockException, ProductNotFoundException {
        Cart cart = cartService.getMyCart();
        Order order = orderService.createOrderFromCart(cart);
        OrderResponse orderResponse = orderService.getOrderDTO(order.getId());
        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        OrderResponse orderResponse = orderService.getOrderDTO(orderId);
        return ResponseEntity.ok(orderResponse);
    }
}