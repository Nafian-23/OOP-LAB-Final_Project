package com.foodapp.ui;

import com.foodapp.model.*;
import com.foodapp.service.OrderService;
import com.foodapp.service.RestaurantService;
import com.foodapp.service.UserService;

import java.util.List;
import java.util.Scanner;

public class AdminUI {
    private final UserService userService;
    private final RestaurantService restaurantService;
    private final OrderService orderService;
    private final Scanner scanner = new Scanner(System.in);

    public AdminUI(UserService userService, RestaurantService restaurantService, OrderService orderService) {
        this.userService = userService;
        this.restaurantService = restaurantService;
        this.orderService = orderService;
    }

    public void start() {
        System.out.println("\n=== Admin Portal ===");
        boolean running = true;
        while (running) {
            System.out.println("\nAdmin Options:");
            System.out.println("1. View All Users");
            System.out.println("2. View All Restaurants");
            System.out.println("3. View All Orders");
            System.out.println("4. Manage Orders (Advance Status)");
            System.out.println("5. Back to Main Menu");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewAllUsers();
                case "2" -> viewAllRestaurants();
                case "3" -> viewAllOrders();
                case "4" -> manageOrders();
                case "5" -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void viewAllUsers() {
        List<User> users = userService.getAllUsers();
        System.out.println("\nAll Users:");
        for (User user : users) {
            System.out.println(user.getId() + " - " + user.getUsername() + " (" + user.getRole() + ")");
        }
    }

    private void viewAllRestaurants() {
        List<Restaurant> restaurants = restaurantService.getAllRestaurants();
        System.out.println("\nAll Restaurants:");
        for (Restaurant r : restaurants) {
            System.out.println(r.getId() + " - " + r.getName() + " (" + r.getAddress() + ")");
        }
    }

    private void viewAllOrders() {
        // Assuming we can get all orders, but OrderService might not have it. Let's add a method.
        // For now, skip or assume.
        System.out.println("All Orders: (Feature to be implemented)");
    }

    private void manageOrders() {
        System.out.print("Enter Order ID: ");
        String orderId = scanner.nextLine().trim();
        try {
            Order order = orderService.getOrder(orderId);
            System.out.println("Current status: " + order.getStatus());
            System.out.print("Advance status? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                orderService.advanceStatus(orderId, null);
                System.out.println("Status advanced.");
            }
        } catch (Exception e) {
            System.out.println("Order not found or error: " + e.getMessage());
        }
    }
}