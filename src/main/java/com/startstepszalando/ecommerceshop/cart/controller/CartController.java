package com.startstepszalando.ecommerceshop.cart.controller;

import com.startstepszalando.ecommerceshop.cart.dto.CartResponse;
import com.startstepszalando.ecommerceshop.cart.service.CartService;
import com.startstepszalando.ecommerceshop.exception.product.ProductNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart", description = "Cart management APIs")
public class CartController {
    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @Operation(summary = "Add product to cart",
            description = "Adds a product to the user's cart. Requires user authentication.",
            security = { @SecurityRequirement(name = "bearerAuth") },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Product added to cart successfully"),
                    @ApiResponse(responseCode = "400", description = "Product not found or insufficient stock",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(oneOf = {ProductNotFoundException.class, IllegalArgumentException.class}))),
                    @ApiResponse(responseCode = "403", description = "Access denied",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AccessDeniedException.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error or unexpected error",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Exception.class)))
            })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/add")
    public ResponseEntity<Void> addProductToCart(@RequestParam Long productId, @RequestParam Integer quantity) throws ProductNotFoundException {
        cartService.addProductToCart(productId, quantity);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Remove product from cart",
            description = "Removes a product from the user's cart. Requires user authentication.",
            security = { @SecurityRequirement(name = "bearerAuth") },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Product removed from cart successfully"),
                    @ApiResponse(responseCode = "403", description = "Access denied",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AccessDeniedException.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error or unexpected error",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Exception.class)))
            })
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/remove")
    public ResponseEntity<Void> removeProductFromCart(@RequestParam Long productId) {
        cartService.removeProductFromCart(productId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get cart details",
            description = "Retrieves the current user's cart details including products, quantities, and total cost. Requires user authentication.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cart details retrieved successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CartResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Access denied",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AccessDeniedException.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error or unexpected error",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Exception.class)))
            })
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<CartResponse> getCartDetails() {
        CartResponse cartDetails = cartService.getMyCartDetails();
        return ResponseEntity.ok(cartDetails);
    }

    @Operation(summary = "Get cart total",
            description = "Calculates the total cost of the current user's cart. Requires user authentication.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cart total cost retrieved successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = BigDecimal.class))),
                    @ApiResponse(responseCode = "403", description = "Access denied",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AccessDeniedException.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error or unexpected error",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Exception.class)))
            })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getCartTotal() {
        BigDecimal totalCost = cartService.calculateTotalCost();
        return ResponseEntity.ok(totalCost);
    }
}
