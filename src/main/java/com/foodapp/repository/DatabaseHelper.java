package com.foodapp.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:foodapp.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id TEXT PRIMARY KEY,
                    username TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    full_name TEXT NOT NULL,
                    address TEXT,
                    role TEXT NOT NULL
                )
                """);

            // Restaurants table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS restaurants (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    address TEXT NOT NULL,
                    cuisine_type TEXT,
                    contact TEXT,
                    opening_hour INTEGER,
                    closing_hour INTEGER,
                    delivery_radius_km REAL,
                    average_rating REAL,
                    estimated_delivery_minutes INTEGER,
                    area TEXT NOT NULL
                )
                """);

            // Menu Items table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS menu_items (
                    id TEXT PRIMARY KEY,
                    restaurant_id TEXT NOT NULL,
                    name TEXT NOT NULL,
                    description TEXT,
                    price REAL NOT NULL,
                    available BOOLEAN DEFAULT 1,
                    stock_quantity INTEGER DEFAULT 0,
                    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
                )
                """);

            // Orders table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS orders (
                    id TEXT PRIMARY KEY,
                    customer_id TEXT NOT NULL,
                    restaurant_id TEXT NOT NULL,
                    status TEXT NOT NULL,
                    subtotal REAL NOT NULL,
                    delivery_fee REAL NOT NULL,
                    discount REAL NOT NULL,
                    total_amount REAL NOT NULL,
                    payment_method TEXT,
                    payment_status TEXT,
                    rider_name TEXT,
                    delivery_address TEXT,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (customer_id) REFERENCES users(id),
                    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
                )
                """);

            // Order Items table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS order_items (
                    id TEXT PRIMARY KEY,
                    order_id TEXT NOT NULL,
                    menu_item_id TEXT NOT NULL,
                    quantity INTEGER NOT NULL,
                    price REAL NOT NULL,
                    FOREIGN KEY (order_id) REFERENCES orders(id),
                    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
                )
                """);

            // Payments table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS payments (
                    id TEXT PRIMARY KEY,
                    order_id TEXT NOT NULL,
                    amount REAL NOT NULL,
                    method TEXT NOT NULL,
                    status TEXT NOT NULL,
                    FOREIGN KEY (order_id) REFERENCES orders(id)
                )
                """);

            // Coupons table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS coupons (
                    id TEXT PRIMARY KEY,
                    code TEXT UNIQUE NOT NULL,
                    discount_percentage REAL NOT NULL,
                    expiry_date DATETIME,
                    is_active BOOLEAN DEFAULT 1
                )
                """);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
}