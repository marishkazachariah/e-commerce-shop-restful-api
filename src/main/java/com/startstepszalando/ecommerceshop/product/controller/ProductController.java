package com.startstepszalando.ecommerceshop.product.controller;

import com.startstepszalando.ecommerceshop.auth.AuthenticationResponse;
import com.startstepszalando.ecommerceshop.exception.product.DuplicateProductException;
import com.startstepszalando.ecommerceshop.exception.product.InsufficientStockException;
import com.startstepszalando.ecommerceshop.exception.product.ProductNotFoundException;
import com.startstepszalando.ecommerceshop.exception.user.UserNotFoundException;
import com.startstepszalando.ecommerceshop.product.dto.ProductPaginationRequest;

import com.startstepszalando.ecommerceshop.product.dto.ProductRequest;
import com.startstepszalando.ecommerceshop.product.model.Product;
import com.startstepszalando.ecommerceshop.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Add a new product", responses = {
            @ApiResponse(responseCode = "200", description = "Product added successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Duplicate product detected",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DuplicateProductException.class))),
            @ApiResponse(responseCode = "400", description = "Insufficient stock.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = InsufficientStockException.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserNotFoundException.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error or unexpected error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Exception.class)))
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Product> addProduct(@Valid @RequestBody ProductRequest productRequest)
            throws DuplicateProductException, InsufficientStockException, AccessDeniedException {
        Product product = convertDtoToEntity(productRequest);
        Product addedProduct = productService.createProduct(product, productRequest.getAdminId());

        return ResponseEntity.ok().body(addedProduct);
    }

    @Operation(summary = "Update an existing product", responses = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Duplicate product detected",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DuplicateProductException.class))),
            @ApiResponse(responseCode = "400", description = "Insufficient stock.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = InsufficientStockException.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductNotFoundException.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserNotFoundException.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error or unexpected error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Exception.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Product> updateProduct(@PathVariable("id") long id,
                                                 @Valid @RequestBody
                                                 ProductRequest productRequest)
            throws ProductNotFoundException, InsufficientStockException, AccessDeniedException {
        Product product = convertDtoToEntity(productRequest);
        Product updatedProduct = productService.updateProduct(id, product, productRequest.getAdminId());

        return ResponseEntity.ok().body(updatedProduct);
    }

    @Operation(summary = "Delete a product by ID", responses = {
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductNotFoundException.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error or unexpected error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Exception.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") long id) throws ProductNotFoundException {
        productService.deleteProduct(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get a product by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Product retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductNotFoundException.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error or unexpected error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Exception.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable("id") long id) throws ProductNotFoundException {
        Product foundProduct = productService.getProductById(id);

        return ResponseEntity.ok().body(foundProduct);
    }

    @Operation(summary = "Retrieve all products", responses = {
            @ApiResponse(responseCode = "200", description = "Products are listed in paginated order",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductNotFoundException.class))),
            @ApiResponse(responseCode = "400", description = "Duplicate product detected",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DuplicateProductException.class))),
            @ApiResponse(responseCode = "400", description = "Insufficient stock.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = InsufficientStockException.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error or unexpected error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Exception.class)))
    })
    @GetMapping
    public ResponseEntity<ProductPaginationRequest> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size)
            throws ProductNotFoundException {
        ProductPaginationRequest productsResponse = new ProductPaginationRequest();
        Page<Product> products = productService.getAllProducts(page, size);

        List<ProductPaginationRequest.ProductRequest> productRequests = products
                .getContent()
                .stream()
                .map(productService::convertToProductRequest)
                .toList();

        productsResponse.setProducts(productRequests);
        productsResponse.setLimit(size);
        productsResponse.setTotalPages(products.getTotalPages());
        productsResponse.setTotalProducts(products.getTotalElements());
        productsResponse.setCurrentPage(products.getPageable().getPageNumber());
        productsResponse.setOffset(products.getPageable().getOffset());

        return ResponseEntity.ok().body(productsResponse);
    }

    private Product convertDtoToEntity(ProductRequest productRequest) {
        Product product = new Product();
        product.setName(productRequest.getName());
        product.setPrice(productRequest.getPrice());
        product.setDescription(productRequest.getDescription());
        product.setStock(productRequest.getStock());

        return product;
    }
}