package com.startstepszalando.ecommerceshop.order.model;

import com.startstepszalando.ecommerceshop.product.model.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "order_product")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Override
    public String toString() {
        return "OrderProduct{" +
                "id=" + id +
                ", orderId=" + (order != null ? order.getId() : "null") +
                ", productId=" + (product != null ? product.getId() : "null") +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderProduct that = (OrderProduct) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(product != null ? product.getId() : null, that.product != null ? that.product.getId() : null) &&
                Objects.equals(order != null ? order.getId() : null, that.order != null ? that.order.getId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, product != null ? product.getId() : null, order != null ? order.getId() : null);
    }
}
