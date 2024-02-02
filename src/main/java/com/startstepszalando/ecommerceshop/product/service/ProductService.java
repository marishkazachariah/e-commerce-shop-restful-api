package com.startstepszalando.ecommerceshop.product.service;

import com.startstepszalando.ecommerceshop.exception.product.DuplicateProductException;
import com.startstepszalando.ecommerceshop.exception.product.InsufficientStockException;
import com.startstepszalando.ecommerceshop.exception.product.ProductNotFoundException;
import com.startstepszalando.ecommerceshop.exception.user.UserNotFoundException;
import com.startstepszalando.ecommerceshop.product.dto.ProductPaginationRequest;
import com.startstepszalando.ecommerceshop.product.model.Product;
import com.startstepszalando.ecommerceshop.product.repository.ProductRepository;
import com.startstepszalando.ecommerceshop.user.model.Role;
import com.startstepszalando.ecommerceshop.user.model.User;
import com.startstepszalando.ecommerceshop.user.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.nio.file.AccessDeniedException;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final UserService userService;

    @Autowired
    public ProductService(ProductRepository repository, UserService userService) {
        this.productRepository = repository;
        this.userService = userService;
    }

    public Product getProductById(long id) throws ProductNotFoundException {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(String.format("Product with id %d is not found", id)));
    }

    public Page<Product> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findAll(pageable);

        if (products.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        return products;
    }

    public Product createProduct(Product product, Long adminId) throws
            DuplicateProductException, InsufficientStockException, AccessDeniedException {
        if (productRepository.hasEntityWithNameAndPrice(product.getName(), product.getPrice())) {
            throw new DuplicateProductException(String.format("Product with name %s and price %.2f already exists", product.getName(), product.getPrice()));
        }

        User admin = userService.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException("Invalid ID: user not found"));

        if (!isAdminUser(admin)) {
            throw new AccessDeniedException("Access denied: You don't have permissions for this action");
        }

        if (product.getStock() <= 0) {
            throw new InsufficientStockException("Insufficient stock for the product");
        }
        product.setAdmin(admin);

        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(long id, Product product, Long adminId) throws
            ProductNotFoundException, InsufficientStockException, AccessDeniedException {
        User admin = userService.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException("Invalid userId: user not found"));

        if (!isAdminUser(admin)) {
            throw new AccessDeniedException("Access denied: You don't have permissions for this action");
        }

        Product productToUpdate = getProductById(id);

        if (product.getStock() <= 0) {
            throw new InsufficientStockException("Insufficient stock for the product");
        }

        productToUpdate.setAdmin(admin);
        product.setId(productToUpdate.getId());

        return productRepository.save(product);
    }

    @Transactional
    public boolean deleteProduct(long id) throws ProductNotFoundException {
        productRepository.delete(getProductById(id));
        return true;
    }

    public ProductPaginationRequest.ProductRequest convertToProductRequest(Product product) {
        ProductPaginationRequest.ProductRequest productRequest = new ProductPaginationRequest.ProductRequest();
        productRequest.setName(product.getName());
        productRequest.setPrice(product.getPrice());
        productRequest.setDescription(product.getDescription());
        productRequest.setStock(productRequest.getStock());
        return productRequest;
    }

    private boolean isAdminUser(User user) {
        Role role = user.getRole();
        return !role.name().equals("ADMIN");
    }
}