package com.foodapp.repository;

import com.foodapp.model.MenuItem;
import com.foodapp.model.Restaurant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RestaurantRepositoryDB {
    private final MenuItemRepositoryDB menuItemRepo = new MenuItemRepositoryDB();

    public void save(Restaurant restaurant) {
        String sql = """
            INSERT OR REPLACE INTO restaurants (id, name, address, cuisine_type, contact,
            opening_hour, closing_hour, delivery_radius_km, average_rating, estimated_delivery_minutes, area)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, restaurant.getId());
            pstmt.setString(2, restaurant.getName());
            pstmt.setString(3, restaurant.getAddress());
            pstmt.setString(4, restaurant.getCuisineType());
            pstmt.setString(5, restaurant.getContact());
            pstmt.setInt(6, restaurant.getOpeningHour());
            pstmt.setInt(7, restaurant.getClosingHour());
            pstmt.setDouble(8, restaurant.getDeliveryRadiusKm());
            pstmt.setDouble(9, restaurant.getAverageRating());
            pstmt.setInt(10, restaurant.getEstimatedDeliveryMinutes());
            pstmt.setString(11, restaurant.getArea() != null ? restaurant.getArea() : "");
            pstmt.executeUpdate();

            // Save menu items
            if (restaurant.getMenuItems() != null) {
                for (MenuItem item : restaurant.getMenuItems()) {
                    menuItemRepo.save(item, restaurant.getId());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save restaurant", e);
        }
    }

    public List<Restaurant> findAll() {
        List<Restaurant> restaurants = new ArrayList<>();
        String sql = "SELECT * FROM restaurants";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Restaurant r = new Restaurant(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("address"),
                    rs.getString("cuisine_type"),
                    rs.getString("contact"),
                    rs.getInt("opening_hour"),
                    rs.getInt("closing_hour"),
                    rs.getDouble("delivery_radius_km"),
                    rs.getDouble("average_rating"),
                    rs.getInt("estimated_delivery_minutes"),
                    rs.getString("area"),
                    menuItemRepo.findByRestaurantId(rs.getString("id"))
                );
                restaurants.add(r);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load restaurants", e);
        }
        return restaurants;
    }

    public Optional<Restaurant> findById(String id) {
        String sql = "SELECT * FROM restaurants WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Restaurant r = new Restaurant(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("address"),
                    rs.getString("cuisine_type"),
                    rs.getString("contact"),
                    rs.getInt("opening_hour"),
                    rs.getInt("closing_hour"),
                    rs.getDouble("delivery_radius_km"),
                    rs.getDouble("average_rating"),
                    rs.getInt("estimated_delivery_minutes"),
                    rs.getString("area"),
                    menuItemRepo.findByRestaurantId(rs.getString("id"))
                );
                return Optional.of(r);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find restaurant", e);
        }
        return Optional.empty();
    }

    public List<Restaurant> findByArea(String area) {
        List<Restaurant> restaurants = new ArrayList<>();
        String sql = "SELECT * FROM restaurants WHERE area LIKE ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + area + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Restaurant r = new Restaurant(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("address"),
                    rs.getString("cuisine_type"),
                    rs.getString("contact"),
                    rs.getInt("opening_hour"),
                    rs.getInt("closing_hour"),
                    rs.getDouble("delivery_radius_km"),
                    rs.getDouble("average_rating"),
                    rs.getInt("estimated_delivery_minutes"),
                    rs.getString("area"),
                    menuItemRepo.findByRestaurantId(rs.getString("id"))
                );
                restaurants.add(r);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find restaurants by area", e);
        }
        return restaurants;
    }

    public void deleteById(String id) {
        String sql = "DELETE FROM restaurants WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete restaurant", e);
        }
    }

    public void update(Restaurant restaurant) {
        save(restaurant);
    }
}