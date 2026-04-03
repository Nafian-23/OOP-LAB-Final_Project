package com.foodapp.service;

import com.foodapp.model.Order;
import com.foodapp.model.Payment;
import com.foodapp.model.PaymentMethod;
import com.foodapp.model.PaymentStatus;
import com.foodapp.repository.OrderRepositoryDB;

public class PaymentService {
    private final OrderRepositoryDB orderRepository;

    public PaymentService(OrderRepositoryDB orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Payment processPayment(Order order, PaymentMethod method) {
        PaymentStatus status;
        if (method == PaymentMethod.CASH_ON_DELIVERY) {
            status = PaymentStatus.PENDING;
        } else {
            status = PaymentStatus.COMPLETED;
        }
        order.setPaymentMethod(method);
        order.setPaymentStatus(status);
        orderRepository.save(order); // Update the order
        return new Payment(order.getId(), method, status);
    }
}
