package com.startstepszalando.ecommerceshop.product.repository;

import com.startstepszalando.ecommerceshop.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean hasEntityWithNameAndPrice(String name, double price);
}