package com.foodapp;

import com.foodapp.api.FoodAppAPI;
import com.foodapp.api.FoodAppWebService;
import com.foodapp.model.Role;
import com.foodapp.repository.*;
import com.foodapp.service.*;
import com.foodapp.ui.CustomerUI;
import com.foodapp.ui.RestaurantUI;

import jakarta.xml.ws.Endpoint;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Init SQLite DB
        DatabaseHelper.initializeDatabase();

        // DB-backed repositories
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

        // Seed admin account
        try {
            userService.register("admin", "admin123", "Administrator", "", Role.ADMIN);
            System.out.println("Admin account created: admin / admin123");
        } catch (IllegalArgumentException ignored) { /* already exists */ }

        // Start SOAP Web Service
        try {
            FoodAppWebService webService = new FoodAppWebService();
            Endpoint.publish("http://localhost:8080/foodapp", webService);
            System.out.println("SOAP Web Service: http://localhost:8080/foodapp?wsdl");
        } catch (Exception e) {
            System.err.println("SOAP service failed to start: " + e.getMessage());
        }

        // Start REST API + Web UI
        FoodAppAPI api = new FoodAppAPI(restaurantService, orderService, userService);
        try {
            api.start(8000);
            System.out.println("Web UI:  http://localhost:8000");
            System.out.println("REST API: http://localhost:8000/api/restaurants");
        } catch (Exception e) {
            System.err.println("HTTP API failed to start: " + e.getMessage());
        }
    }
}
