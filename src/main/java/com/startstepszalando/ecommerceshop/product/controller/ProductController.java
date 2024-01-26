package com.startstepszalando.ecommerceshop.product.controller;

import com.startstepszalando.ecommerceshop.product.dto.ProductRequest;
import com.startstepszalando.ecommerceshop.product.model.Product;
import jakarta.validation.Valid;
import org.hibernate.query.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ProductRequest> getProducts(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "3") int size){
        ProductRequest productsResponse = new ProductRequest();

        return ResponseEntity.ok().body(productsResponse);
    }
}