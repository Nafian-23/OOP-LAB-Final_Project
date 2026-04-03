package com.foodapp.repository;

import com.foodapp.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderRepositoryDB {
    public void save(Order order) {
        String sql = """
            INSERT OR REPLACE INTO orders (id, customer_id, restaurant_id, status, subtotal, delivery_fee, discount, total_amount,
            payment_method, payment_status, rider_name, delivery_address, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, order.getId());
            pstmt.setString(2, order.getCustomerId());
            pstmt.setString(3, order.getRestaurantId());
            pstmt.setString(4, order.getStatus().name());
            pstmt.setDouble(5, order.getSubtotal());
            pstmt.setDouble(6, order.getDeliveryFee());
            pstmt.setDouble(7, order.getDiscount());
            pstmt.setDouble(8, order.getTotal());
            pstmt.setString(9, order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null);
            pstmt.setString(10, order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null);
            pstmt.setString(11, order.getRiderName());
            pstmt.setString(12, ""); // delivery address, assume from user
            pstmt.setString(13, order.getCreatedAt() != null ? order.getCreatedAt().toString() : LocalDateTime.now().toString());
            pstmt.executeUpdate();

            // Save order items
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    saveOrderItem(order.getId(), item);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save order", e);
        }
    }

    private void saveOrderItem(String orderId, OrderItem item) throws SQLException {
        String sql = "INSERT OR REPLACE INTO order_items (id, order_id, menu_item_id, quantity, price) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String itemId = java.util.UUID.randomUUID().toString();
            pstmt.setString(1, itemId);
            pstmt.setString(2, orderId);
            pstmt.setString(3, item.getMenuItem().getId());
            pstmt.setInt(4, item.getQuantity());
            pstmt.setDouble(5, item.getMenuItem().getPrice());
            pstmt.executeUpdate();
        }
    }

    public Optional<Order> findById(String id) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Order order = new Order(
                    rs.getString("id"),
                    rs.getString("customer_id"),
                    rs.getString("restaurant_id"),
                    getOrderItems(rs.getString("id")),
                    rs.getDouble("subtotal"),
                    rs.getDouble("delivery_fee"),
                    rs.getDouble("discount"),
                    rs.getDouble("total_amount"),
                    OrderStatus.valueOf(rs.getString("status")),
                    rs.getString("payment_method") != null ? PaymentMethod.valueOf(rs.getString("payment_method")) : null,
                    rs.getString("payment_status") != null ? PaymentStatus.valueOf(rs.getString("payment_status")) : null,
                    rs.getString("rider_name"),
                    LocalDateTime.parse(rs.getString("created_at"))
                );
                return Optional.of(order);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find order", e);
        }
        return Optional.empty();
    }

    private List<OrderItem> getOrderItems(String orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.*, mi.name, mi.price FROM order_items oi JOIN menu_items mi ON oi.menu_item_id = mi.id WHERE oi.order_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                MenuItem menuItem = new MenuItem();
                menuItem.setId(rs.getString("menu_item_id"));
                menuItem.setName(rs.getString("name"));
                menuItem.setPrice(rs.getDouble("price"));
                OrderItem item = new OrderItem(menuItem, rs.getInt("quantity"));
                items.add(item);
            }
        }
        return items;
    }

    public List<Order> findByRestaurantId(String restaurantId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE restaurant_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, restaurantId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Order order = new Order(
                    rs.getString("id"),
                    rs.getString("customer_id"),
                    rs.getString("restaurant_id"),
                    getOrderItems(rs.getString("id")),
                    rs.getDouble("subtotal"),
                    rs.getDouble("delivery_fee"),
                    rs.getDouble("discount"),
                    rs.getDouble("total_amount"),
                    OrderStatus.valueOf(rs.getString("status")),
                    rs.getString("payment_method") != null ? PaymentMethod.valueOf(rs.getString("payment_method")) : null,
                    rs.getString("payment_status") != null ? PaymentStatus.valueOf(rs.getString("payment_status")) : null,
                    rs.getString("rider_name"),
                    LocalDateTime.parse(rs.getString("created_at"))
                );
                orders.add(order);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find orders by restaurant", e);
        }
        return orders;
    }

    public List<Order> findByCustomerId(String customerId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE customer_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Order order = new Order(
                    rs.getString("id"),
                    rs.getString("customer_id"),
                    rs.getString("restaurant_id"),
                    getOrderItems(rs.getString("id")),
                    rs.getDouble("subtotal"),
                    rs.getDouble("delivery_fee"),
                    rs.getDouble("discount"),
                    rs.getDouble("total_amount"),
                    OrderStatus.valueOf(rs.getString("status")),
                    rs.getString("payment_method") != null ? PaymentMethod.valueOf(rs.getString("payment_method")) : null,
                    rs.getString("payment_status") != null ? PaymentStatus.valueOf(rs.getString("payment_status")) : null,
                    rs.getString("rider_name"),
                    LocalDateTime.parse(rs.getString("created_at"))
                );
                orders.add(order);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find orders by customer", e);
        }
        return orders;
    }

    public void updateStatus(String orderId, OrderStatus status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setString(2, orderId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update order status", e);
        }
    }
}