package com.startstepszalando.ecommerceshop.order.repository;

import com.startstepszalando.ecommerceshop.order.model.Order;
import com.startstepszalando.ecommerceshop.order.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);

     List<Order> findByStatus(OrderStatus status);

    Page<Order> findAllByUserId(Long userId, Pageable pageable);
}
