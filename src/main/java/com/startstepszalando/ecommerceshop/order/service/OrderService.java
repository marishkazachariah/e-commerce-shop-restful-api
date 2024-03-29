package com.startstepszalando.ecommerceshop.order.service;

import com.startstepszalando.ecommerceshop.cart.model.Cart;
import com.startstepszalando.ecommerceshop.cart.model.CartItem;
import com.startstepszalando.ecommerceshop.cart.service.CartService;
import com.startstepszalando.ecommerceshop.exception.cart.EmptyCartException;
import com.startstepszalando.ecommerceshop.exception.order.OrderNotFoundException;
import com.startstepszalando.ecommerceshop.exception.product.InsufficientStockException;
import com.startstepszalando.ecommerceshop.exception.product.ProductNotFoundException;
import com.startstepszalando.ecommerceshop.exception.user.UserNotFoundException;
import com.startstepszalando.ecommerceshop.order.dto.OrderProductResponse;
import com.startstepszalando.ecommerceshop.order.dto.OrderResponse;
import com.startstepszalando.ecommerceshop.order.model.Order;
import com.startstepszalando.ecommerceshop.order.model.OrderProduct;
import com.startstepszalando.ecommerceshop.order.model.OrderStatus;
import com.startstepszalando.ecommerceshop.order.repository.OrderProductRepository;
import com.startstepszalando.ecommerceshop.order.repository.OrderRepository;
import com.startstepszalando.ecommerceshop.product.service.ProductService;
import com.startstepszalando.ecommerceshop.user.model.User;
import com.startstepszalando.ecommerceshop.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {
    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductService productService;
    private final UserService userService;

    public OrderService(CartService cartService, OrderRepository orderRepository,
                        OrderProductRepository orderProductRepository,
                        ProductService productService, UserService userService) {
        this.cartService = cartService;
        this.orderRepository = orderRepository;
        this.orderProductRepository = orderProductRepository;
        this.productService = productService;
        this.userService = userService;
    }

    @Transactional
    public Order createOrderFromCart(Cart cart) throws InsufficientStockException, EmptyCartException, UserNotFoundException, ProductNotFoundException {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new EmptyCartException("Cannot create order from an empty cart.");
        }

        BigDecimal totalCost = cartService.calculateTotalCost();
        if (totalCost.compareTo(BigDecimal.ZERO) <= 0) {
            throw new EmptyCartException("Cannot create order from an empty cart.");
        }

        User user = cart.getUser();
        if (user == null || !userService.isValidUserDetails(user)) {
            throw new UserNotFoundException("User details are incomplete or invalid");
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(totalCost);

        Order savedOrder = orderRepository.saveAndFlush(order);

        for (CartItem cartItem : cart.getItems()) {
            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setOrder(savedOrder);
            orderProduct.setProduct(cartItem.getProduct());
            orderProduct.setQuantity(cartItem.getQuantity());
            orderProduct.setPrice(cartItem.getProduct().getPrice());

            productService.updateProductStock(cartItem.getProduct().getId(), cartItem.getQuantity());

            savedOrder.getProducts().add(orderProduct);
            orderProduct.setOrder(savedOrder);

            orderProductRepository.save(orderProduct);
        }

        cartService.clearCart(cart);

        return savedOrder;
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) throws OrderNotFoundException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDTO(Long orderId, String username) throws AccessDeniedException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        if (!order.getUser().getEmail().equals(username) && userService.isAdmin()) {
            throw new AccessDeniedException("Not authorized to view this order");
        }

        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setId(order.getId());
        orderResponse.setOrderDate(order.getOrderDate());
        orderResponse.setTotalPrice(order.getTotalPrice());
        orderResponse.setStatus(order.getStatus().toString());

        List<OrderProductResponse> productDTOs = order.getProducts().stream()
                .map(this::convertToOrderProductDTO)
                .toList();
        orderResponse.setProducts(productDTOs);

        return orderResponse;
    }

    public Page<OrderResponse> getAllOrdersForUser(Long userId, int page, int size) throws ProductNotFoundException, AccessDeniedException {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findAllByUserId(userId, pageable);

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        if (orders.stream().anyMatch(order -> !order.getUser().getEmail().equals(currentUsername)) && userService.isAdmin()) {
            throw new AccessDeniedException("Not authorized to view orders for this user");
        }

        if (orders.isEmpty()) {
            throw new ProductNotFoundException(
                    String.format("No orders found for user with ID %d", userId));
        }

        return orders.map(this::convertToOrderResponseDTO);
    }

    private OrderResponse convertToOrderResponseDTO(Order order) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setId(order.getId());
        orderResponse.setOrderDate(order.getOrderDate());
        orderResponse.setTotalPrice(order.getTotalPrice());
        orderResponse.setStatus(order.getStatus().toString());
        orderResponse.setProducts(order.getProducts().stream()
                .map(this::convertToOrderProductResponseDTO)
                .toList());
        return orderResponse;
    }

    private OrderProductResponse convertToOrderProductResponseDTO(OrderProduct orderProduct) {
        OrderProductResponse productResponse = new OrderProductResponse();
        productResponse.setProductId(orderProduct.getId());
        return productResponse;
    }

    private OrderProductResponse convertToOrderProductDTO(OrderProduct orderProduct) {
        OrderProductResponse response = new OrderProductResponse();
        response.setProductId(orderProduct.getProduct().getId());
        response.setProductName(orderProduct.getProduct().getName());
        response.setQuantity(orderProduct.getQuantity());
        response.setPrice(orderProduct.getPrice());
        return response;
    }
}