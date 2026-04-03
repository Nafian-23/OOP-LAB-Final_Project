package com.foodapp.repository;

import com.foodapp.model.MenuItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MenuItemRepositoryDB {
    public void save(MenuItem item, String restaurantId) {
        String sql = "INSERT OR REPLACE INTO menu_items (id, restaurant_id, name, description, price, available, stock_quantity) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getId());
            pstmt.setString(2, restaurantId);
            pstmt.setString(3, item.getName());
            pstmt.setString(4, item.getDescription());
            pstmt.setDouble(5, item.getPrice());
            pstmt.setBoolean(6, item.isAvailable());
            pstmt.setInt(7, item.getStockQuantity());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save menu item", e);
        }
    }

    public List<MenuItem> findByRestaurantId(String restaurantId) {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT * FROM menu_items WHERE restaurant_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, restaurantId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                MenuItem item = new MenuItem(
                    rs.getString("id"),
                    rs.getString("restaurant_id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getString("description"),
                    rs.getBoolean("available"),
                    rs.getInt("stock_quantity"),
                    new ArrayList<>() // customizationOptions not stored
                );
                items.add(item);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load menu items", e);
        }
        return items;
    }
}