package com.startstepszalando.ecommerceshop.product.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.startstepszalando.ecommerceshop.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @JsonIgnore
    @Transient
    private final int MAX_QUANTITY = 10000;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer stock;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "admin_id")
    private User admin;

    public boolean reduceQuantity(int quantity) {
        if (this.stock < quantity) return false;
        this.stock -= quantity;
        return true;
    }

    public boolean increaseQuantity(int quantity){
        if (this.stock + quantity > MAX_QUANTITY) return false;
        this.stock += quantity;
        return true;
    }
}
