package com.foodapp.service;

import com.foodapp.model.*;
import com.foodapp.repository.OrderRepositoryDB;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class OrderService {
    public static final double DELIVERY_FEE = 50.0;

    private final OrderRepositoryDB orderRepository;
    private final PaymentService paymentService;

    public OrderService(OrderRepositoryDB orderRepository, PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
    }

    public Order createOrder(String customerId, String restaurantId, List<OrderItem> cartItems,
                             Coupon coupon, PaymentMethod paymentMethod) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty. Please add items before placing an order.");
        }

        double subtotal = cartItems.stream()
                .mapToDouble(item -> item.getMenuItem().getPrice() * item.getQuantity())
                .sum();

        double discount = (coupon != null) ? subtotal * coupon.getDiscountPercent() / 100.0 : 0.0;
        double total = subtotal + DELIVERY_FEE - discount;

        String id = UUID.randomUUID().toString();
        Order order = new Order(id, customerId, restaurantId, cartItems,
                subtotal, DELIVERY_FEE, discount, total,
                OrderStatus.PLACED, paymentMethod, PaymentStatus.PENDING,
                null, LocalDateTime.now());

        orderRepository.save(order);
        paymentService.processPayment(order, paymentMethod);
        return order;
    }

    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

    public List<Order> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public Order advanceStatus(String orderId, String restaurantId) {
        Order order = getOrder(orderId);
        switch (order.getStatus()) {
            case PLACED:
                order.setStatus(OrderStatus.PREPARING);
                break;
            case PREPARING:
                order.setStatus(OrderStatus.READY);
                break;
            default:
                throw new IllegalStateException(
                        "Cannot advance order status from " + order.getStatus());
        }
        orderRepository.updateStatus(orderId, order.getStatus());
        return order;
    }

    public List<Order> getOrdersByRestaurant(String restaurantId) {
        return orderRepository.findByRestaurantId(restaurantId).stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public Order assignRider(String orderId, String riderName) {
        Order order = getOrder(orderId);
        if (order.getStatus() != OrderStatus.READY) {
            throw new IllegalStateException(
                    "Cannot assign rider: order status is " + order.getStatus() + ", expected READY");
        }
        order.setRiderName(riderName);
        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.updateStatus(orderId, order.getStatus());
        // TODO: update rider name in DB
        return order;
    }
}
