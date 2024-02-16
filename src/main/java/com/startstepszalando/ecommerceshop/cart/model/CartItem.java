package com.startstepszalando.ecommerceshop.cart.model;

import com.startstepszalando.ecommerceshop.product.model.Product;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Table(name = "cart_item")
@IdClass(CartItemId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    @Id
    @ManyToOne
    @JoinColumn(name = "cart_id", referencedColumnName = "id", nullable = false)
    private Cart cart;

    @Id
    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Override
    public String toString() {
        return "CartItem{" +
                "cartId=" + (cart != null ? cart.getId() : "null") +
                ", productId=" + (product != null ? product.getId() : "null") +
                ", quantity=" + quantity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CartItem)) return false;
        CartItem that = (CartItem) o;
        return Objects.equals(product != null ? product.getId() : null, that.product != null ? that.product.getId() : null) &&
                Objects.equals(cart != null ? cart.getId() : null, that.cart != null ? that.cart.getId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(product != null ? product.getId() : null, cart != null ? cart.getId() : null);
    }
}
