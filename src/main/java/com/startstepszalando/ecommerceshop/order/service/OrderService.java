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
import com.startstepszalando.ecommerceshop.product.model.Product;
import com.startstepszalando.ecommerceshop.product.service.ProductService;
import com.startstepszalando.ecommerceshop.user.model.User;
import com.startstepszalando.ecommerceshop.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Transactional
    public Order createOrderFromCart(Cart cart) throws InsufficientStockException, ProductNotFoundException {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new EmptyCartException("Cannot create order from an empty cart.");
        }

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (product.getStock() < item.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }
        }

        User user = cart.getUser();
        if (user == null || !userService.isValidUserDetails(user)) {
            throw new UserNotFoundException("User details are incomplete or invalid");
        }

        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(cartService.calculateTotalCost());

        Order savedOrder = orderRepository.save(order);

        for (CartItem cartItem : cart.getItems()) {
            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setOrder(savedOrder);
            orderProduct.setProduct(cartItem.getProduct());
            orderProduct.setQuantity(cartItem.getQuantity());
            orderProduct.setPrice(cartItem.getProduct().getPrice());

            productService.updateProductStock(cartItem.getProduct().getId(), cartItem.getQuantity());

            orderProductRepository.save(orderProduct);
        }

        cartService.clearCart(cart);

        return savedOrder;
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDTO(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

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

    private OrderProductResponse convertToOrderProductDTO(OrderProduct orderProduct) {
        OrderProductResponse response = new OrderProductResponse();
        response.setProductId(orderProduct.getProduct().getId());
        response.setProductName(orderProduct.getProduct().getName());
        response.setQuantity(orderProduct.getQuantity());
        response.setPrice(orderProduct.getPrice());
        return response;
    }
}

