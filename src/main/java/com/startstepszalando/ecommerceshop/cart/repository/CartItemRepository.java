package com.startstepszalando.ecommerceshop.cart.repository;

import com.startstepszalando.ecommerceshop.cart.dto.CartItemResponse;
import com.startstepszalando.ecommerceshop.cart.model.CartItem;
import com.startstepszalando.ecommerceshop.cart.model.CartItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, CartItemId> {
    @Query("SELECT new com.startstepszalando.ecommerceshop.cart.dto.CartItemResponse(" +
            "p.id, p.name, ci.quantity, p.price) " +
            "FROM Cart c " +
            "JOIN c.items ci " +
            "JOIN ci.product p " +
            "WHERE c.user.id = :userId")
    List<CartItemResponse> findCartDetailsByUserId(@Param("userId") Long userId);

    void deleteByCartId(Long cartId);
}
