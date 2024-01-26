package com.startstepszalando.ecommerceshop.product.service;

import com.startstepszalando.ecommerceshop.exception.product.ProductNotFoundException;
import com.startstepszalando.ecommerceshop.product.model.Product;
import com.startstepszalando.ecommerceshop.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository repository){
        this.productRepository = repository;
    }
    public Product getById(long id) throws ProductNotFoundException {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(String.format("Product with id %d is not found", id)));
    }

    public Page<Product> getAllProducts(int page, int size) throws ProductNotFoundException {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findAll(pageable);
        if(products.isEmpty()){
            throw new ProductNotFoundException(
                    String.format("Page %d not found. Products has %d pages",
                            products.getPageable().getPageNumber(), products.getTotalPages()));
        }
        return products;
    }
}