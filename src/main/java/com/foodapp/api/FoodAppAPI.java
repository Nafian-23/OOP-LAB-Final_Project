package com.foodapp.api;

import com.foodapp.service.OrderService;
import com.foodapp.service.RestaurantService;
import com.foodapp.service.UserService;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class FoodAppAPI {
    private final RestaurantService restaurantService;
    private final OrderService orderService;
    private final UserService userService;

    public FoodAppAPI(RestaurantService restaurantService, OrderService orderService, UserService userService) {
        this.restaurantService = restaurantService;
        this.orderService = orderService;
        this.userService = userService;
    }

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/", new APIHandler(restaurantService, orderService, userService));
        server.createContext("/", new StaticFileHandler("web"));
        server.setExecutor(null);
        server.start();
    }
}
