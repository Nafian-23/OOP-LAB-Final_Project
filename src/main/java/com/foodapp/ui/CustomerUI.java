package com.foodapp.ui;

import com.foodapp.model.*;
import com.foodapp.service.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CustomerUI {
    private final UserService userService;
    private final RestaurantService restaurantService;
    private final OrderService orderService;
    private final SearchService searchService;
    private final CouponService couponService;

    private final Scanner scanner;
    private User currentUser;
    private final List<OrderItem> cart = new ArrayList<>();
    private Coupon activeCoupon;

    public CustomerUI(UserService userService, RestaurantService restaurantService,
                      OrderService orderService, SearchService searchService,
                      CouponService couponService) {
        this.userService = userService;
        this.restaurantService = restaurantService;
        this.orderService = orderService;
        this.searchService = searchService;
        this.couponService = couponService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("\n=== Customer Portal ===");
        authFlow();
        if (currentUser == null) return;
        mainMenuLoop();
    }

    private void authFlow() {
        while (currentUser == null) {
            System.out.println("\n1. Login\n2. Register\n3. Back");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> loginFlow();
                case "2" -> registerFlow();
                case "3" -> { return; }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void loginFlow() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        try {
            currentUser = userService.login(username, password);
            System.out.println("Welcome, " + currentUser.getFullName() + "!");
        } catch (AuthException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    private void registerFlow() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Full Name: ");
        String fullName = scanner.nextLine().trim();
        System.out.print("Delivery Address: ");
        String address = scanner.nextLine().trim();
        try {
            currentUser = userService.register(username, password, fullName, address, Role.CUSTOMER);
            System.out.println("Registered successfully! Welcome, " + currentUser.getFullName() + "!");
        } catch (IllegalArgumentException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private void mainMenuLoop() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== Main Menu ===");
            System.out.println("1. Browse Restaurants");
            System.out.println("2. Search");
            System.out.println("3. View Cart");
            System.out.println("4. Place Order");
            System.out.println("5. View Orders");
            System.out.println("6. Apply Coupon");
            System.out.println("7. Exit");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> browseRestaurants();
                case "2" -> searchFlow();
                case "3" -> viewCart();
                case "4" -> placeOrder();
                case "5" -> viewOrders();
                case "6" -> applyCoupon();
                case "7" -> running = false;
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void browseRestaurants() {
        List<Restaurant> restaurants = restaurantService.findByArea(currentUser.getAddress());
        if (restaurants.isEmpty()) {
            System.out.println("No restaurants available in your area.");
            return;
        }

        System.out.println("\nSort by: 1. Rating  2. Delivery Time  3. Name  4. No sort");
        System.out.print("Choice: ");
        String sortChoice = scanner.nextLine().trim();
        switch (sortChoice) {
            case "1" -> restaurants = restaurantService.sortByRating();
            case "2" -> restaurants = restaurantService.sortByDeliveryTime();
            case "3" -> restaurants = restaurantService.sortByName();
            default -> { /* no sort */ }
        }

        System.out.println("\n--- Restaurants ---");
        for (int i = 0; i < restaurants.size(); i++) {
            Restaurant r = restaurants.get(i);
            System.out.printf("%d. %s | %s | Rating: %.1f | Delivery: %d min%n",
                    i + 1, r.getName(), r.getCuisineType(), r.getAverageRating(), r.getEstimatedDeliveryMinutes());
        }

        System.out.print("Select restaurant (0 to go back): ");
        try {
            int idx = Integer.parseInt(scanner.nextLine().trim());
            if (idx > 0 && idx <= restaurants.size()) {
                viewMenu(restaurants.get(idx - 1));
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    private void viewMenu(Restaurant restaurant) {
        List<MenuItem> items = restaurant.getMenuItems();
        if (items.isEmpty()) {
            System.out.println("No menu items available.");
            return;
        }
        System.out.println("\n--- Menu: " + restaurant.getName() + " ---");
        for (int i = 0; i < items.size(); i++) {
            MenuItem item = items.get(i);
            String status = item.isAvailable() ? "Available" : "Unavailable";
            System.out.printf("%d. %s | Rs %.2f | %s | %s%n",
                    i + 1, item.getName(), item.getPrice(), item.getDescription(), status);
        }

        System.out.print("Select item to add to cart (0 to go back): ");
        try {
            int idx = Integer.parseInt(scanner.nextLine().trim());
            if (idx > 0 && idx <= items.size()) {
                MenuItem selected = items.get(idx - 1);
                if (!selected.isAvailable()) {
                    System.out.println("This item is currently unavailable.");
                    return;
                }
                if (selected.getStockQuantity() <= 0) {
                    System.out.println("This item is out of stock.");
                    return;
                }
                System.out.print("Quantity: ");
                try {
                    int qty = Integer.parseInt(scanner.nextLine().trim());
                    if (qty <= 0) {
                        System.out.println("Quantity must be greater than zero.");
                        return;
                    }
                    cart.add(new OrderItem(selected, qty));
                    System.out.println(selected.getName() + " x" + qty + " added to cart.");
                } catch (NumberFormatException e) {
                    System.out.println("Invalid quantity.");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    private void searchFlow() {
        System.out.print("Search query: ");
        String query = scanner.nextLine().trim();

        List<Restaurant> restaurants = searchService.searchRestaurants(query);
        System.out.println("\n--- Matching Restaurants ---");
        if (restaurants.isEmpty()) {
            System.out.println("No restaurants found.");
        } else {
            restaurants.forEach(r -> System.out.printf("  %s (%s)%n", r.getName(), r.getCuisineType()));
        }

        List<MenuItem> items = searchService.searchMenuItems(query);
        System.out.println("\n--- Matching Menu Items ---");
        if (items.isEmpty()) {
            System.out.println("No menu items found.");
        } else {
            items.forEach(i -> System.out.printf("  %s | Rs %.2f | %s%n",
                    i.getName(), i.getPrice(), i.isAvailable() ? "Available" : "Unavailable"));
        }
    }

    private void viewCart() {
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }
        System.out.println("\n--- Cart ---");
        double subtotal = 0;
        for (OrderItem item : cart) {
            double lineTotal = item.getMenuItem().getPrice() * item.getQuantity();
            subtotal += lineTotal;
            System.out.printf("  %s x%d = Rs %.2f%n",
                    item.getMenuItem().getName(), item.getQuantity(), lineTotal);
        }
        double discount = activeCoupon != null ? subtotal * activeCoupon.getDiscountPercent() / 100.0 : 0;
        double total = subtotal + OrderService.DELIVERY_FEE - discount;
        System.out.printf("Subtotal: Rs %.2f%n", subtotal);
        System.out.printf("Delivery Fee: Rs %.2f%n", OrderService.DELIVERY_FEE);
        if (activeCoupon != null) {
            System.out.printf("Discount (%s): -Rs %.2f%n", activeCoupon.getCode(), discount);
        }
        System.out.printf("Total: Rs %.2f%n", total);
    }

    private void placeOrder() {
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty. Please add items before placing an order.");
            return;
        }

        String restaurantId = cart.get(0).getMenuItem().getRestaurantId();

        System.out.println("Payment method: 1. Cash on Delivery  2. Card");
        System.out.print("Choice: ");
        PaymentMethod paymentMethod;
        try {
            int pmChoice = Integer.parseInt(scanner.nextLine().trim());
            paymentMethod = (pmChoice == 2) ? PaymentMethod.CARD : PaymentMethod.CASH_ON_DELIVERY;
        } catch (NumberFormatException e) {
            paymentMethod = PaymentMethod.CASH_ON_DELIVERY;
        }

        try {
            Order order = orderService.createOrder(currentUser.getId(), restaurantId,
                    new ArrayList<>(cart), activeCoupon, paymentMethod);
            System.out.println("Order placed successfully! Order ID: " + order.getId());
            System.out.printf("Total: Rs %.2f | Payment: %s | Status: %s%n",
                    order.getTotal(), order.getPaymentMethod(), order.getPaymentStatus());
            cart.clear();
            activeCoupon = null;
        } catch (Exception e) {
            System.out.println("Failed to place order: " + e.getMessage());
        }
    }

    private void viewOrders() {
        List<Order> orders = orderService.getOrdersByCustomer(currentUser.getId());
        if (orders.isEmpty()) {
            System.out.println("No orders found.");
            return;
        }
        System.out.println("\n--- Your Orders ---");
        for (Order order : orders) {
            System.out.printf("ID: %s | Status: %s | Total: Rs %.2f | Created: %s%n",
                    order.getId(), order.getStatus(), order.getTotal(), order.getCreatedAt());
        }
    }

    private void applyCoupon() {
        System.out.print("Enter coupon code: ");
        String code = scanner.nextLine().trim();
        try {
            activeCoupon = couponService.validate(code);
            System.out.println("Coupon applied: " + activeCoupon.getDiscountPercent() + "% discount!");
        } catch (CouponException e) {
            System.out.println("Coupon error: " + e.getMessage());
        }
    }
}
