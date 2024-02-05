package com.startstepszalando.ecommerceshop.cart.model;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CartItemId implements Serializable {
    private Long cart;
    private Long product;
}
