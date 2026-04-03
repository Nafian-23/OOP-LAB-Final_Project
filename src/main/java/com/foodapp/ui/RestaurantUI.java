package com.foodapp.ui;

import com.foodapp.model.*;
import com.foodapp.service.OrderService;
import com.foodapp.service.RestaurantService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class RestaurantUI {
    private final RestaurantService restaurantService;
    private final OrderService orderService;
    private final Scanner scanner;

    private static final List<String> RIDERS = Arrays.asList("Alice", "Bob", "Charlie", "Diana", "Eve");

    private Restaurant currentRestaurant;

    public RestaurantUI(RestaurantService restaurantService, OrderService orderService) {
        this.restaurantService = restaurantService;
        this.orderService = orderService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("\n=== Restaurant Portal ===");
        restaurantFlow();
        if (currentRestaurant == null) return;
        mainMenuLoop();
    }

    private void restaurantFlow() {
        while (currentRestaurant == null) {
            System.out.println("\n1. Register Restaurant\n2. Select Existing Restaurant\n3. Back");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> registerRestaurant();
                case "2" -> selectRestaurant();
                case "3" -> { return; }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void registerRestaurant() {
        System.out.print("Restaurant Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Address: ");
        String address = scanner.nextLine().trim();
        System.out.print("Cuisine Type: ");
        String cuisine = scanner.nextLine().trim();
        System.out.print("Contact: ");
        String contact = scanner.nextLine().trim();
        System.out.print("Area: ");
        String area = scanner.nextLine().trim();
        int openingHour = readInt("Opening Hour (0-23): ");
        int closingHour = readInt("Closing Hour (0-23): ");
        double radius = readDouble("Delivery Radius (km): ");

        try {
            currentRestaurant = restaurantService.register(name, address, cuisine, contact,
                    openingHour, closingHour, radius, area);
            System.out.println("Restaurant registered: " + currentRestaurant.getName());
        } catch (IllegalArgumentException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private void selectRestaurant() {
        List<Restaurant> all = restaurantService.getAllRestaurants();
        if (all.isEmpty()) {
            System.out.println("No restaurants registered yet.");
            return;
        }
        System.out.println("\n--- Registered Restaurants ---");
        for (int i = 0; i < all.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, all.get(i).getName());
        }
        System.out.print("Select (0 to cancel): ");
        try {
            int idx = Integer.parseInt(scanner.nextLine().trim());
            if (idx > 0 && idx <= all.size()) {
                currentRestaurant = all.get(idx - 1);
                System.out.println("Selected: " + currentRestaurant.getName());
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    private void mainMenuLoop() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== Restaurant Menu: " + currentRestaurant.getName() + " ===");
            System.out.println("1. Manage Menu");
            System.out.println("2. View Orders");
            System.out.println("3. Update Order Status");
            System.out.println("4. Exit");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> manageMenu();
                case "2" -> viewOrders();
                case "3" -> updateOrderStatus();
                case "4" -> running = false;
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void manageMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n--- Manage Menu ---");
            System.out.println("1. Add Item");
            System.out.println("2. Update Availability");
            System.out.println("3. Update Stock");
            System.out.println("4. Back");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> addMenuItem();
                case "2" -> updateAvailability();
                case "3" -> updateStock();
                case "4" -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void addMenuItem() {
        System.out.print("Item Name: ");
        String name = scanner.nextLine().trim();
        double price = readDouble("Price: ");
        System.out.print("Description: ");
        String description = scanner.nextLine().trim();
        int stock = readInt("Stock Quantity: ");
        System.out.print("Available (y/n): ");
        boolean available = scanner.nextLine().trim().equalsIgnoreCase("y");
        System.out.print("Customization options (comma-separated, or blank): ");
        String customInput = scanner.nextLine().trim();
        List<String> customizations = new ArrayList<>();
        if (!customInput.isBlank()) {
            for (String opt : customInput.split(",")) {
                customizations.add(opt.trim());
            }
        }

        // Refresh restaurant from service to get latest state
        currentRestaurant = restaurantService.getRestaurant(currentRestaurant.getId());
        MenuItem item = restaurantService.addMenuItem(currentRestaurant.getId(), name, price,
                description, stock, available, customizations);
        currentRestaurant = restaurantService.getRestaurant(currentRestaurant.getId());
        System.out.println("Menu item added: " + item.getName());
    }

    private void updateAvailability() {
        currentRestaurant = restaurantService.getRestaurant(currentRestaurant.getId());
        List<MenuItem> items = currentRestaurant.getMenuItems();
        if (items.isEmpty()) {
            System.out.println("No menu items.");
            return;
        }
        printMenuItems(items);
        System.out.print("Select item (0 to cancel): ");
        try {
            int idx = Integer.parseInt(scanner.nextLine().trim());
            if (idx > 0 && idx <= items.size()) {
                MenuItem item = items.get(idx - 1);
                System.out.print("Available (y/n): ");
                boolean available = scanner.nextLine().trim().equalsIgnoreCase("y");
                restaurantService.updateMenuItemAvailability(currentRestaurant.getId(), item.getId(), available);
                currentRestaurant = restaurantService.getRestaurant(currentRestaurant.getId());
                System.out.println("Availability updated.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    private void updateStock() {
        currentRestaurant = restaurantService.getRestaurant(currentRestaurant.getId());
        List<MenuItem> items = currentRestaurant.getMenuItems();
        if (items.isEmpty()) {
            System.out.println("No menu items.");
            return;
        }
        printMenuItems(items);
        System.out.print("Select item (0 to cancel): ");
        try {
            int idx = Integer.parseInt(scanner.nextLine().trim());
            if (idx > 0 && idx <= items.size()) {
                MenuItem item = items.get(idx - 1);
                int qty = readInt("New Stock Quantity: ");
                restaurantService.updateMenuItemStock(currentRestaurant.getId(), item.getId(), qty);
                currentRestaurant = restaurantService.getRestaurant(currentRestaurant.getId());
                System.out.println("Stock updated.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    private void viewOrders() {
        List<Order> orders = orderService.getOrdersByRestaurant(currentRestaurant.getId());
        List<Order> placed = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PLACED)
                .toList();
        if (placed.isEmpty()) {
            System.out.println("No pending orders.");
            return;
        }
        System.out.println("\n--- Pending Orders ---");
        for (Order order : placed) {
            System.out.printf("ID: %s | Total: Rs %.2f | Items: %d%n",
                    order.getId(), order.getTotal(), order.getItems().size());
        }
    }

    private void updateOrderStatus() {
        List<Order> orders = orderService.getOrdersByRestaurant(currentRestaurant.getId());
        if (orders.isEmpty()) {
            System.out.println("No orders found.");
            return;
        }
        System.out.println("\n--- Orders ---");
        for (int i = 0; i < orders.size(); i++) {
            Order o = orders.get(i);
            System.out.printf("%d. ID: %s | Status: %s | Total: Rs %.2f%n",
                    i + 1, o.getId(), o.getStatus(), o.getTotal());
        }
        System.out.print("Select order (0 to cancel): ");
        int idx;
        try {
            idx = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }
        if (idx <= 0 || idx > orders.size()) return;

        Order order = orders.get(idx - 1);
        System.out.println("Current status: " + order.getStatus());
        System.out.println("1. Accept (PLACED→PREPARING)");
        System.out.println("2. Mark Ready (PREPARING→READY)");
        System.out.println("3. Assign Rider (READY→DELIVERED)");
        System.out.print("Action: ");
        String action = scanner.nextLine().trim();

        try {
            switch (action) {
                case "1" -> {
                    Order updated = orderService.advanceStatus(order.getId(), currentRestaurant.getId());
                    System.out.println("Order status updated to: " + updated.getStatus());
                }
                case "2" -> {
                    Order updated = orderService.advanceStatus(order.getId(), currentRestaurant.getId());
                    System.out.println("Order status updated to: " + updated.getStatus());
                }
                case "3" -> {
                    System.out.println("Select rider:");
                    for (int i = 0; i < RIDERS.size(); i++) {
                        System.out.printf("%d. %s%n", i + 1, RIDERS.get(i));
                    }
                    System.out.print("Choice: ");
                    try {
                        int riderIdx = Integer.parseInt(scanner.nextLine().trim());
                        if (riderIdx > 0 && riderIdx <= RIDERS.size()) {
                            String riderName = RIDERS.get(riderIdx - 1);
                            Order updated = orderService.assignRider(order.getId(), riderName);
                            System.out.println("Rider " + riderName + " assigned. Status: " + updated.getStatus());
                        } else {
                            System.out.println("Invalid rider selection.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input.");
                    }
                }
                default -> System.out.println("Invalid action.");
            }
        } catch (IllegalStateException e) {
            System.out.println("Cannot update status: " + e.getMessage());
        }
    }

    private void printMenuItems(List<MenuItem> items) {
        for (int i = 0; i < items.size(); i++) {
            MenuItem item = items.get(i);
            System.out.printf("%d. %s | Rs %.2f | %s | Stock: %d%n",
                    i + 1, item.getName(), item.getPrice(),
                    item.isAvailable() ? "Available" : "Unavailable", item.getStockQuantity());
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}
