package com.foodapp.api;

import com.foodapp.model.*;
import com.foodapp.service.OrderService;
import com.foodapp.service.RestaurantService;
import com.foodapp.service.UserService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class APIHandler implements HttpHandler {

    private final RestaurantService restaurantService;
    private final OrderService orderService;
    private final UserService userService;

    public APIHandler(RestaurantService restaurantService, OrderService orderService, UserService userService) {
        this.restaurantService = restaurantService;
        this.orderService = orderService;
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // CORS preflight
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String method = exchange.getRequestMethod().toUpperCase();
        String path   = exchange.getRequestURI().getPath();
        String query  = exchange.getRequestURI().getQuery();

        try {
            // ── GET routes ──────────────────────────────────────────────
            if ("GET".equals(method)) {

                if (path.equals("/api/restaurants")) {
                    String area = queryParam(query, "area");
                    List<Restaurant> list = restaurantService.findByArea(area == null ? "" : area);
                    sendJson(exchange, 200, restaurantsToJson(list));

                } else if (path.matches("/api/restaurant/[^/]+/menu")) {
                    String id = path.split("/")[3];
                    try {
                        Restaurant r = restaurantService.getRestaurant(id);
                        sendJson(exchange, 200, menuItemsToJson(r.getMenuItems()));
                    } catch (IllegalArgumentException e) {
                        sendJson(exchange, 404, err("Restaurant not found: " + id));
                    }

                } else if (path.equals("/api/restaurants/all")) {
                    sendJson(exchange, 200, restaurantsToJson(restaurantService.getAllRestaurants()));

                } else if (path.matches("/api/order/[^/]+/status")) {
                    String id = path.split("/")[3];
                    try {
                        Order o = orderService.getOrder(id);
                        sendJson(exchange, 200, orderStatusToJson(o));
                    } catch (IllegalArgumentException e) {
                        sendJson(exchange, 404, err("Order not found: " + id));
                    }

                } else if (path.matches("/api/restaurant/[^/]+/orders")) {
                    String restaurantId = path.split("/")[3];
                    List<Order> orders = orderService.getOrdersByRestaurant(restaurantId);
                    sendJson(exchange, 200, ordersToJson(orders));

                } else {
                    sendJson(exchange, 404, err("Not found"));
                }

            // ── POST routes ─────────────────────────────────────────────
            } else if ("POST".equals(method)) {

                String body = readBody(exchange);

                if (path.equals("/api/orders")) {
                    handlePlaceOrder(exchange, body);

                } else if (path.matches("/api/order/[^/]+/advance")) {
                    String orderId = path.split("/")[3];
                    try {
                        Order o = orderService.advanceStatus(orderId, null);
                        sendJson(exchange, 200, orderStatusToJson(o));
                    } catch (IllegalStateException e) {
                        sendJson(exchange, 400, err(e.getMessage()));
                    } catch (IllegalArgumentException e) {
                        sendJson(exchange, 404, err(e.getMessage()));
                    }

                } else if (path.matches("/api/order/[^/]+/assign-rider")) {
                    String orderId = path.split("/")[3];
                    String rider = jsonField(body, "rider");
                    if (rider == null || rider.isBlank()) rider = "Alice";
                    try {
                        Order o = orderService.assignRider(orderId, rider);
                        sendJson(exchange, 200, orderStatusToJson(o));
                    } catch (IllegalStateException e) {
                        sendJson(exchange, 400, err(e.getMessage()));
                    } catch (IllegalArgumentException e) {
                        sendJson(exchange, 404, err(e.getMessage()));
                    }

                } else if (path.equals("/api/restaurant/register")) {
                    handleRegisterRestaurant(exchange, body);

                } else if (path.matches("/api/restaurant/[^/]+/menu/add")) {
                    String restaurantId = path.split("/")[3];
                    handleAddMenuItem(exchange, body, restaurantId);

                } else if (path.matches("/api/restaurant/[^/]+/menu/[^/]+/availability")) {
                    String[] parts = path.split("/");
                    String restaurantId = parts[3];
                    String itemId = parts[5];
                    String availStr = jsonField(body, "available");
                    boolean avail = "true".equalsIgnoreCase(availStr);
                    restaurantService.updateMenuItemAvailability(restaurantId, itemId, avail);
                    sendJson(exchange, 200, "{\"ok\":true}");

                } else if (path.equals("/api/users/register")) {
                    handleRegisterUser(exchange, body);

                } else if (path.equals("/api/users/login")) {
                    handleLoginUser(exchange, body);

                } else {
                    sendJson(exchange, 404, err("Not found"));
                }

            } else {
                sendJson(exchange, 405, err("Method Not Allowed"));
            }

        } catch (Exception e) {
            sendJson(exchange, 500, err("Internal server error: " + e.getMessage()));
        }
    }

    // ── Handler helpers ──────────────────────────────────────────────────

    private void handlePlaceOrder(HttpExchange exchange, String body) throws IOException {
        String customerId  = jsonField(body, "customerId");
        String restaurantId = jsonField(body, "restaurantId");
        String payStr      = jsonField(body, "paymentMethod");
        String itemsJson   = jsonArrayField(body, "items");

        if (customerId == null || restaurantId == null || itemsJson == null) {
            sendJson(exchange, 400, err("Missing required fields: customerId, restaurantId, items"));
            return;
        }

        PaymentMethod pm = "CARD".equalsIgnoreCase(payStr)
                ? PaymentMethod.CARD : PaymentMethod.CASH_ON_DELIVERY;

        List<OrderItem> cartItems = parseOrderItems(itemsJson, restaurantId);
        if (cartItems.isEmpty()) {
            sendJson(exchange, 400, err("Cart is empty or items not found"));
            return;
        }

        Order order = orderService.createOrder(customerId, restaurantId, cartItems, null, pm);
        sendJson(exchange, 201, orderToJson(order));
    }

    private void handleRegisterRestaurant(HttpExchange exchange, String body) throws IOException {
        String name     = jsonField(body, "name");
        String address  = jsonField(body, "address");
        String cuisine  = jsonField(body, "cuisineType");
        String contact  = jsonField(body, "contact");
        String openStr  = jsonField(body, "openingHour");
        String closeStr = jsonField(body, "closingHour");
        String radStr   = jsonField(body, "deliveryRadiusKm");
        String area     = jsonField(body, "area");

        if (name == null || address == null || cuisine == null) {
            sendJson(exchange, 400, err("Missing required fields"));
            return;
        }
        int open  = openStr  != null ? parseInt(openStr,  9)  : 9;
        int close = closeStr != null ? parseInt(closeStr, 22) : 22;
        double rad = radStr  != null ? parseDouble(radStr, 5.0) : 5.0;

        try {
            Restaurant r = restaurantService.register(name, address, cuisine,
                    contact != null ? contact : "", open, close, rad,
                    area != null ? area : "");
            sendJson(exchange, 201, restaurantToJson(r));
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 409, err(e.getMessage()));
        }
    }

    private void handleAddMenuItem(HttpExchange exchange, String body, String restaurantId) throws IOException {
        String name   = jsonField(body, "name");
        String desc   = jsonField(body, "description");
        String priceS = jsonField(body, "price");
        String stockS = jsonField(body, "stockQuantity");
        String availS = jsonField(body, "available");

        if (name == null || priceS == null) {
            sendJson(exchange, 400, err("Missing required fields: name, price"));
            return;
        }
        double price = parseDouble(priceS, 0.0);
        int stock    = parseInt(stockS, 10);
        boolean avail = !"false".equalsIgnoreCase(availS);

        try {
            MenuItem item = restaurantService.addMenuItem(restaurantId, name, price,
                    desc != null ? desc : "", stock, avail, new ArrayList<>());
            sendJson(exchange, 201, menuItemToJson(item));
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 404, err(e.getMessage()));
        }
    }

    private void handleRegisterUser(HttpExchange exchange, String body) throws IOException {
        String username = jsonField(body, "username");
        String password = jsonField(body, "password");
        String fullName = jsonField(body, "fullName");
        String address  = jsonField(body, "address");
        if (username == null || password == null) {
            sendJson(exchange, 400, err("Missing username or password"));
            return;
        }
        try {
            User u = userService.register(username, password,
                    fullName != null ? fullName : username,
                    address  != null ? address  : "",
                    Role.CUSTOMER);
            sendJson(exchange, 201, userToJson(u));
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 409, err(e.getMessage()));
        }
    }

    private void handleLoginUser(HttpExchange exchange, String body) throws IOException {
        String username = jsonField(body, "username");
        String password = jsonField(body, "password");
        if (username == null || password == null) {
            sendJson(exchange, 400, err("Missing username or password"));
            return;
        }
        try {
            User u = userService.login(username, password);
            sendJson(exchange, 200, userToJson(u));
        } catch (Exception e) {
            sendJson(exchange, 401, err("Invalid credentials"));
        }
    }

    // ── JSON serialisers ─────────────────────────────────────────────────

    private String restaurantsToJson(List<Restaurant> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(restaurantToJson(list.get(i)));
        }
        return sb.append("]").toString();
    }

    private String restaurantToJson(Restaurant r) {
        return "{\"id\":\"" + esc(r.getId()) + "\","
             + "\"name\":\"" + esc(r.getName()) + "\","
             + "\"address\":\"" + esc(r.getAddress()) + "\","
             + "\"cuisineType\":\"" + esc(r.getCuisineType()) + "\","
             + "\"contact\":\"" + esc(r.getContact()) + "\","
             + "\"averageRating\":" + r.getAverageRating() + ","
             + "\"estimatedDeliveryMinutes\":" + r.getEstimatedDeliveryMinutes() + "}";
    }

    private String menuItemsToJson(List<MenuItem> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(menuItemToJson(list.get(i)));
        }
        return sb.append("]").toString();
    }

    private String menuItemToJson(MenuItem item) {
        return "{\"id\":\"" + esc(item.getId()) + "\","
             + "\"restaurantId\":\"" + esc(item.getRestaurantId()) + "\","
             + "\"name\":\"" + esc(item.getName()) + "\","
             + "\"price\":" + item.getPrice() + ","
             + "\"description\":\"" + esc(item.getDescription()) + "\","
             + "\"available\":" + item.isAvailable() + ","
             + "\"stockQuantity\":" + item.getStockQuantity() + "}";
    }

    private String orderStatusToJson(Order o) {
        return "{\"id\":\"" + esc(o.getId()) + "\","
             + "\"status\":\"" + o.getStatus() + "\","
             + "\"paymentStatus\":\"" + o.getPaymentStatus() + "\"}";
    }

    private String orderToJson(Order o) {
        return "{\"id\":\"" + esc(o.getId()) + "\","
             + "\"customerId\":\"" + esc(o.getCustomerId()) + "\","
             + "\"restaurantId\":\"" + esc(o.getRestaurantId()) + "\","
             + "\"subtotal\":" + o.getSubtotal() + ","
             + "\"deliveryFee\":" + o.getDeliveryFee() + ","
             + "\"discount\":" + o.getDiscount() + ","
             + "\"total\":" + o.getTotal() + ","
             + "\"status\":\"" + o.getStatus() + "\","
             + "\"paymentMethod\":\"" + o.getPaymentMethod() + "\","
             + "\"paymentStatus\":\"" + o.getPaymentStatus() + "\","
             + "\"createdAt\":\"" + o.getCreatedAt() + "\"}";
    }

    private String ordersToJson(List<Order> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(orderToJson(list.get(i)));
        }
        return sb.append("]").toString();
    }

    private String userToJson(User u) {
        return "{\"id\":\"" + esc(u.getId()) + "\","
             + "\"username\":\"" + esc(u.getUsername()) + "\","
             + "\"fullName\":\"" + esc(u.getFullName()) + "\","
             + "\"address\":\"" + esc(u.getAddress()) + "\"}";
    }

    // ── Utility ──────────────────────────────────────────────────────────

    private void sendJson(HttpExchange exchange, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** Very small JSON field extractor — handles simple string and number values. */
    private String jsonField(String json, String key) {
        if (json == null) return null;
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx + search.length());
        if (colon < 0) return null;
        int start = colon + 1;
        while (start < json.length() && json.charAt(start) == ' ') start++;
        if (start >= json.length()) return null;
        if (json.charAt(start) == '"') {
            int end = json.indexOf('"', start + 1);
            return end < 0 ? null : json.substring(start + 1, end);
        }
        // number / boolean
        int end = start;
        while (end < json.length() && ",}]".indexOf(json.charAt(end)) < 0) end++;
        return json.substring(start, end).trim();
    }

    /** Extracts a raw JSON array value for a key, e.g. "items":[...] */
    private String jsonArrayField(String json, String key) {
        if (json == null) return null;
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx + search.length());
        if (colon < 0) return null;
        int start = json.indexOf('[', colon);
        if (start < 0) return null;
        int depth = 0, end = start;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (c == '[') depth++;
            else if (c == ']') { depth--; if (depth == 0) break; }
            end++;
        }
        return json.substring(start, end + 1);
    }

    /**
     * Parses the items array from the web cart.
     * Expected format: [{"itemId":"...","quantity":2}, ...]
     */
    private List<OrderItem> parseOrderItems(String itemsJson, String restaurantId) {
        List<OrderItem> result = new ArrayList<>();
        try {
            Restaurant restaurant = restaurantService.getRestaurant(restaurantId);
            // Split on object boundaries
            String inner = itemsJson.trim();
            if (inner.startsWith("[")) inner = inner.substring(1);
            if (inner.endsWith("]")) inner = inner.substring(0, inner.length() - 1);

            // Split by },{
            String[] objects = inner.split("\\},\\s*\\{");
            for (String obj : objects) {
                obj = obj.replace("{", "").replace("}", "").trim();
                String itemId = jsonField("{" + obj + "}", "itemId");
                String qtyStr = jsonField("{" + obj + "}", "quantity");
                if (itemId == null) continue;
                int qty = qtyStr != null ? parseInt(qtyStr, 1) : 1;
                restaurant.getMenuItems().stream()
                        .filter(m -> m.getId().equals(itemId) && m.isAvailable())
                        .findFirst()
                        .ifPresent(m -> result.add(new OrderItem(m, qty)));
            }
        } catch (Exception ignored) {}
        return result;
    }

    private String queryParam(String query, String key) {
        if (query == null) return null;
        for (String p : query.split("&")) {
            if (p.startsWith(key + "=")) {
                try { return URLDecoder.decode(p.substring(key.length() + 1), StandardCharsets.UTF_8); }
                catch (Exception e) { return p.substring(key.length() + 1); }
            }
        }
        return null;
    }

    private String err(String msg) { return "{\"error\":\"" + esc(msg) + "\"}"; }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private double parseDouble(String s, double def) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return def; }
    }
}
