package com.foodapp;

import java.util.Scanner;

import com.foodapp.api.FoodAppAPI;
import com.foodapp.api.FoodAppWebService;
import com.foodapp.repository.DatabaseHelper;
import com.foodapp.repository.OrderRepositoryDB;
import com.foodapp.repository.RestaurantRepositoryDB;
import com.foodapp.repository.UserRepositoryDB;
import com.foodapp.service.CouponService;
import com.foodapp.service.OrderService;
import com.foodapp.service.PaymentService;
import com.foodapp.service.RestaurantService;
import com.foodapp.service.SearchService;
import com.foodapp.service.UserService;
import com.foodapp.ui.AdminUI;
import com.foodapp.ui.CustomerUI;
import com.foodapp.ui.RestaurantUI;

import jakarta.xml.ws.Endpoint;

public class Main {
    public static void main(String[] args) {
        // Initialize database
        DatabaseHelper.initializeDatabase();

        // Repositories
        UserRepositoryDB userRepository = new UserRepositoryDB();
        RestaurantRepositoryDB restaurantRepository = new RestaurantRepositoryDB();
        OrderRepositoryDB orderRepository = new OrderRepositoryDB();

        // Services
        UserService userService = new UserService(userRepository);
        RestaurantService restaurantService = new RestaurantService(restaurantRepository);
        SearchService searchService = new SearchService(restaurantRepository);
        CouponService couponService = new CouponService();
        PaymentService paymentService = new PaymentService(orderRepository);
        OrderService orderService = new OrderService(orderRepository, paymentService);

        // Start SOAP Web Service
        FoodAppWebService webService = new FoodAppWebService();
        Endpoint.publish("http://localhost:8080/foodapp", webService);
        System.out.println("SOAP Web Service running on http://localhost:8080/foodapp?wsdl");

        // Start HTTP API (optional, for backward compatibility)
        FoodAppAPI api = new FoodAppAPI(restaurantService, orderService, userService);
        try {
            api.start(8000);
            System.out.println("HTTP API running on http://localhost:8000");
        } catch (Exception e) {
            System.err.println("Could not start HTTP API server: " + e.getMessage());
        }

        // Top-level menu
        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                System.out.println("\n=== Food Delivery App ===");
            System.out.println("1. Customer Portal");
            System.out.println("2. Restaurant Portal");
            System.out.println("3. Admin Portal");
            System.out.println("4. Exit");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> {
                    CustomerUI customerUI = new CustomerUI(userService, restaurantService,
                            orderService, searchService, couponService);
                    customerUI.start();
                }
                case "2" -> {
                    RestaurantUI restaurantUI = new RestaurantUI(restaurantService, orderService);
                    restaurantUI.start();
                }
                case "3" -> {
                    AdminUI adminUI = new AdminUI(userService, restaurantService, orderService);
                    adminUI.start();
                }
                case "4" -> running = false;
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
        }
        System.out.println("Goodbye!");
    }
}
