package com.startstepszalando.ecommerceshop.order.controller;

import com.startstepszalando.ecommerceshop.cart.model.Cart;
import com.startstepszalando.ecommerceshop.cart.service.CartService;
import com.startstepszalando.ecommerceshop.exception.order.OrderNotFoundException;
import com.startstepszalando.ecommerceshop.exception.product.InsufficientStockException;
import com.startstepszalando.ecommerceshop.exception.product.ProductNotFoundException;
import com.startstepszalando.ecommerceshop.order.dto.OrderResponse;
import com.startstepszalando.ecommerceshop.order.model.Order;
import com.startstepszalando.ecommerceshop.order.model.OrderStatus;
import com.startstepszalando.ecommerceshop.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order", description = "Order management APIs")
public class OrderController {
    private final OrderService orderService;
    private final CartService cartService;

    public OrderController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @Operation(summary = "Create order from cart",
            description = "Creates an order from the current user's cart. Requires authentication.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order created successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = OrderResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Insufficient stock or product not found",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(oneOf = {InsufficientStockException.class, ProductNotFoundException.class}))),
                    @ApiResponse(responseCode = "403", description = "Access denied",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AccessDeniedException.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error or unexpected error",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Exception.class)))
            })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/createOrderFromCart")
    public ResponseEntity<?> createOrderFromCart() throws InsufficientStockException, ProductNotFoundException, AccessDeniedException {
        Cart cart = cartService.getMyCart();
        Order order = orderService.createOrderFromCart(cart);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        OrderResponse orderResponse = orderService.getOrderDTO(order.getId(), currentUsername);

        return ResponseEntity.ok(orderResponse);
    }


    @Operation(summary = "Get order by ID",
            description = "Returns a single order by its ID. Requires user authentication and ownership of the order or admin privileges.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order retrieved successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = OrderResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Access denied",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AccessDeniedException.class))),
                    @ApiResponse(responseCode = "404", description = "Order not found",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = OrderNotFoundException.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error or unexpected error",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Exception.class)))
            })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId, Authentication authentication) throws AccessDeniedException {
        String currentUsername = authentication.getName();
        OrderResponse orderResponse = orderService.getOrderDTO(orderId, currentUsername);
        return ResponseEntity.ok(orderResponse);
    }

    @Operation(summary = "Retrieve all orders for a specific user",
            description = "Returns all orders for a specified user in a paginated view. Users who placed the orders can only access their past orders.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Orders are listed in paginated order",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "403", description = "Access denied",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AccessDeniedException.class))),
                    @ApiResponse(responseCode = "404", description = "No orders found for user",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ProductNotFoundException.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error or unexpected error",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Exception.class)))
            })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<OrderResponse>> getAllOrdersForUser(@PathVariable Long userId,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "3") int size) throws ProductNotFoundException, AccessDeniedException {
        Page<OrderResponse> orders = orderService.getAllOrdersForUser(userId, page, size);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Update order status",
            description = "Updates the status of an existing order. Requires ADMIN role.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order status updated successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Order not found",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = OrderNotFoundException.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error or unexpected error",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Exception.class)))
            })
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{orderId}/updateStatus")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId, @RequestParam OrderStatus newStatus) {
        try {
            orderService.updateOrderStatus(orderId, newStatus);
            return ResponseEntity.ok("Order status updated successfully.");
        } catch (OrderNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}