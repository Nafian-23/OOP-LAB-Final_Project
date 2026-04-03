package com.foodapp.model;

import java.io.Serializable;

public class Payment implements Serializable {
    private static final long serialVersionUID = 1L;

    private String orderId;
    private PaymentMethod method;
    private PaymentStatus status;

    public Payment() {}

    public Payment(String orderId, PaymentMethod method, PaymentStatus status) {
        this.orderId = orderId;
        this.method = method;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "Payment{orderId='" + orderId + "', method=" + method + ", status=" + status + "}";
    }
}
