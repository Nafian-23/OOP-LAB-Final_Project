package com.foodapp.api;

import com.foodapp.model.*;
import com.foodapp.service.*;
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
    private final CouponService couponService;

    public APIHandler(RestaurantService restaurantService, OrderService orderService, UserService userService) {
        this.restaurantService = restaurantService;
        this.orderService = orderService;
        this.userService = userService;
        this.couponService = new CouponService();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        String method = exchange.getRequestMethod().toUpperCase();
        String path   = exchange.getRequestURI().getPath();
        String query  = exchange.getRequestURI().getQuery();
        try {
            if      ("GET".equals(method))    handleGet(exchange, path, query);
            else if ("POST".equals(method))   handlePost(exchange, path, readBody(exchange));
            else if ("DELETE".equals(method)) handleDelete(exchange, path);
            else sendJson(exchange, 405, err("Method Not Allowed"));
        } catch (Exception e) {
            sendJson(exchange, 500, err("Internal server error: " + e.getMessage()));
        }
    }

    private void handleGet(HttpExchange ex, String path, String query) throws IOException {
        if (path.equals("/api/restaurants")) {
            String area = queryParam(query, "area");
            sendJson(ex, 200, restaurantsToJson(restaurantService.findByArea(area == null ? "" : area)));
        } else if (path.equals("/api/restaurants/all")) {
            sendJson(ex, 200, restaurantsToJson(restaurantService.getAllRestaurants()));
        } else if (path.matches("/api/restaurant/[^/]+/menu")) {
            String id = path.split("/")[3];
            try { sendJson(ex, 200, menuItemsToJson(restaurantService.getRestaurant(id).getMenuItems())); }
            catch (IllegalArgumentException e) { sendJson(ex, 404, err("Restaurant not found: " + id)); }
        } else if (path.matches("/api/restaurant/[^/]+/orders")) {
            sendJson(ex, 200, ordersToJson(orderService.getOrdersByRestaurant(path.split("/")[3])));
        } else if (path.matches("/api/order/[^/]+/status")) {
            String id = path.split("/")[3];
            try { sendJson(ex, 200, orderStatusToJson(orderService.getOrder(id))); }
            catch (IllegalArgumentException e) { sendJson(ex, 404, err("Order not found: " + id)); }
        } else if (path.equals("/api/admin/users")) {
            sendJson(ex, 200, usersToJson(userService.getAllUsers()));
        } else if (path.equals("/api/admin/restaurants")) {
            sendJson(ex, 200, restaurantsToJson(restaurantService.getAllRestaurants()));
        } else if (path.equals("/api/coupons")) {
            sendJson(ex, 200, couponsToJson(couponService.getAllCoupons()));
        } else {
            sendJson(ex, 404, err("Not found"));
        }
    }

    private void handlePost(HttpExchange ex, String path, String body) throws IOException {
        if (path.equals("/api/orders")) {
            handlePlaceOrder(ex, body);
        } else if (path.matches("/api/order/[^/]+/advance")) {
            String orderId = path.split("/")[3];
            try { sendJson(ex, 200, orderStatusToJson(orderService.advanceStatus(orderId, null))); }
            catch (IllegalStateException e) { sendJson(ex, 400, err(e.getMessage())); }
            catch (IllegalArgumentException e) { sendJson(ex, 404, err(e.getMessage())); }
        } else if (path.matches("/api/order/[^/]+/assign-rider")) {
            String orderId = path.split("/")[3];
            String rider = jsonField(body, "rider");
            if (rider == null || rider.isBlank()) rider = "Alice";
            try { sendJson(ex, 200, orderStatusToJson(orderService.assignRider(orderId, rider))); }
            catch (IllegalStateException e) { sendJson(ex, 400, err(e.getMessage())); }
            catch (IllegalArgumentException e) { sendJson(ex, 404, err(e.getMessage())); }
        } else if (path.equals("/api/restaurant/register") || path.equals("/api/admin/restaurant/add")) {
            handleRegisterRestaurant(ex, body);
        } else if (path.matches("/api/restaurant/[^/]+/menu/add")) {
            handleAddMenuItem(ex, body, path.split("/")[3]);
        } else if (path.matches("/api/restaurant/[^/]+/menu/[^/]+/availability")) {
            String[] p = path.split("/");
            restaurantService.updateMenuItemAvailability(p[3], p[5], "true".equalsIgnoreCase(jsonField(body, "available")));
            sendJson(ex, 200, "{\"ok\":true}");
        } else if (path.equals("/api/users/register")) {
            handleRegisterUser(ex, body);
        } else if (path.equals("/api/users/login")) {
            handleLoginUser(ex, body);
        } else if (path.equals("/api/admin/login")) {
            handleAdminLogin(ex, body);
        } else if (path.equals("/api/coupons/validate")) {
            handleValidateCoupon(ex, body);
        } else {
            sendJson(ex, 404, err("Not found"));
        }
    }

    private void handleDelete(HttpExchange ex, String path) throws IOException {
        if (path.matches("/api/admin/users/[^/]+")) {
            try { userService.deleteUser(path.split("/")[4]); sendJson(ex, 200, "{\"ok\":true}"); }
            catch (Exception e) { sendJson(ex, 404, err(e.getMessage())); }
        } else if (path.matches("/api/admin/restaurants/[^/]+")) {
            try { restaurantService.deleteRestaurant(path.split("/")[4]); sendJson(ex, 200, "{\"ok\":true}"); }
            catch (Exception e) { sendJson(ex, 404, err(e.getMessage())); }
        } else {
            sendJson(ex, 404, err("Not found"));
        }
    }

    private void handlePlaceOrder(HttpExchange ex, String body) throws IOException {
        String customerId   = jsonField(body, "customerId");
        String restaurantId = jsonField(body, "restaurantId");
        String payStr       = jsonField(body, "paymentMethod");
        String couponCode   = jsonField(body, "couponCode");
        String itemsJson    = jsonArrayField(body, "items");
        if (customerId == null || restaurantId == null || itemsJson == null) {
            sendJson(ex, 400, err("Missing required fields")); return;
        }
        PaymentMethod pm = "CARD".equalsIgnoreCase(payStr) ? PaymentMethod.CARD : PaymentMethod.CASH_ON_DELIVERY;
        Coupon coupon = null;
        if (couponCode != null && !couponCode.isBlank()) {
            try { coupon = couponService.validate(couponCode); } catch (Exception ignored) {}
        }
        List<OrderItem> items = parseOrderItems(itemsJson, restaurantId);
        if (items.isEmpty()) { sendJson(ex, 400, err("Cart is empty or items not found")); return; }
        Order order = orderService.createOrder(customerId, restaurantId, items, coupon, pm);
        sendJson(ex, 201, orderToJson(order));
    }

    private void handleRegisterRestaurant(HttpExchange ex, String body) throws IOException {
        String name    = jsonField(body, "name");
        String address = jsonField(body, "address");
        String cuisine = jsonField(body, "cuisineType");
        String contact = jsonField(body, "contact");
        String area    = jsonField(body, "area");
        int open  = parseInt(jsonField(body, "openingHour"),  9);
        int close = parseInt(jsonField(body, "closingHour"), 22);
        double rad = parseDouble(jsonField(body, "deliveryRadiusKm"), 5.0);
        if (name == null || address == null || cuisine == null) {
            sendJson(ex, 400, err("Missing required fields")); return;
        }
        try {
            Restaurant r = restaurantService.register(name, address, cuisine,
                    contact != null ? contact : "", open, close, rad, area != null ? area : address);
            sendJson(ex, 201, restaurantToJson(r));
        } catch (IllegalArgumentException e) { sendJson(ex, 409, err(e.getMessage())); }
    }

    private void handleAddMenuItem(HttpExchange ex, String body, String restaurantId) throws IOException {
        String name   = jsonField(body, "name");
        String priceS = jsonField(body, "price");
        if (name == null || priceS == null) { sendJson(ex, 400, err("Missing name or price")); return; }
        double price = parseDouble(priceS, 0.0);
        int stock    = parseInt(jsonField(body, "stockQuantity"), 10);
        boolean avail = !"false".equalsIgnoreCase(jsonField(body, "available"));
        String desc   = jsonField(body, "description");
        try {
            MenuItem item = restaurantService.addMenuItem(restaurantId, name, price,
                    desc != null ? desc : "", stock, avail, new ArrayList<>());
            sendJson(ex, 201, menuItemToJson(item));
        } catch (IllegalArgumentException e) { sendJson(ex, 404, err(e.getMessage())); }
    }

    private void handleRegisterUser(HttpExchange ex, String body) throws IOException {
        String username = jsonField(body, "username");
        String password = jsonField(body, "password");
        if (username == null || password == null) { sendJson(ex, 400, err("Missing username or password")); return; }
        try {
            User u = userService.register(username, password,
                    nvl(jsonField(body, "fullName"), username),
                    nvl(jsonField(body, "address"), ""), Role.CUSTOMER);
            sendJson(ex, 201, userToJson(u));
        } catch (IllegalArgumentException e) { sendJson(ex, 409, err(e.getMessage())); }
    }

    private void handleLoginUser(HttpExchange ex, String body) throws IOException {
        String username = jsonField(body, "username");
        String password = jsonField(body, "password");
        if (username == null || password == null) { sendJson(ex, 400, err("Missing credentials")); return; }
        try { sendJson(ex, 200, userToJson(userService.login(username, password))); }
        catch (Exception e) { sendJson(ex, 401, err("Invalid credentials")); }
    }

    private void handleAdminLogin(HttpExchange ex, String body) throws IOException {
        String username = jsonField(body, "username");
        String password = jsonField(body, "password");
        if (username == null || password == null) { sendJson(ex, 400, err("Missing credentials")); return; }
        try {
            User u = userService.login(username, password);
            if (u.getRole() == Role.ADMIN) {
                sendJson(ex, 200, "{\"ok\":true,\"role\":\"ADMIN\",\"id\":\"" + esc(u.getId()) + "\",\"username\":\"" + esc(u.getUsername()) + "\"}");
            } else {
                sendJson(ex, 403, err("Not an admin account"));
            }
        } catch (Exception e) { sendJson(ex, 401, err("Invalid credentials")); }
    }

    private void handleValidateCoupon(HttpExchange ex, String body) throws IOException {
        String code = jsonField(body, "code");
        if (code == null) { sendJson(ex, 400, err("Missing coupon code")); return; }
        try {
            Coupon c = couponService.validate(code);
            sendJson(ex, 200, "{\"code\":\"" + esc(c.getCode()) + "\",\"discountPercent\":" + c.getDiscountPercent() + ",\"active\":" + c.isActive() + "}");
        } catch (CouponException e) { sendJson(ex, 400, err(e.getMessage())); }
    }

    // ── JSON serialisers ──────────────────────────────────────────────────

    private String restaurantsToJson(List<Restaurant> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) { if (i > 0) sb.append(","); sb.append(restaurantToJson(list.get(i))); }
        return sb.append("]").toString();
    }

    private String restaurantToJson(Restaurant r) {
        int seed = Math.abs(r.getName().hashCode() % 1000);
        return "{\"id\":\"" + esc(r.getId()) + "\","
             + "\"name\":\"" + esc(r.getName()) + "\","
             + "\"address\":\"" + esc(r.getAddress()) + "\","
             + "\"cuisineType\":\"" + esc(r.getCuisineType()) + "\","
             + "\"contact\":\"" + esc(r.getContact()) + "\","
             + "\"averageRating\":" + r.getAverageRating() + ","
             + "\"estimatedDeliveryMinutes\":" + r.getEstimatedDeliveryMinutes() + ","
             + "\"imageUrl\":\"https://picsum.photos/seed/r" + seed + "/400/200\"}";
    }

    private String menuItemsToJson(List<MenuItem> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) { if (i > 0) sb.append(","); sb.append(menuItemToJson(list.get(i))); }
        return sb.append("]").toString();
    }

    private String menuItemToJson(MenuItem item) {
        int seed = Math.abs(item.getName().hashCode() % 1000) + 200;
        return "{\"id\":\"" + esc(item.getId()) + "\","
             + "\"restaurantId\":\"" + esc(item.getRestaurantId()) + "\","
             + "\"name\":\"" + esc(item.getName()) + "\","
             + "\"price\":" + item.getPrice() + ","
             + "\"description\":\"" + esc(item.getDescription()) + "\","
             + "\"available\":" + item.isAvailable() + ","
             + "\"stockQuantity\":" + item.getStockQuantity() + ","
             + "\"imageUrl\":\"https://picsum.photos/seed/m" + seed + "/200/150\"}";
    }

    private String orderStatusToJson(Order o) {
        return "{\"id\":\"" + esc(o.getId()) + "\",\"status\":\"" + o.getStatus() + "\",\"paymentStatus\":\"" + o.getPaymentStatus() + "\"}";
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
        for (int i = 0; i < list.size(); i++) { if (i > 0) sb.append(","); sb.append(orderToJson(list.get(i))); }
        return sb.append("]").toString();
    }

    private String userToJson(User u) {
        return "{\"id\":\"" + esc(u.getId()) + "\","
             + "\"username\":\"" + esc(u.getUsername()) + "\","
             + "\"fullName\":\"" + esc(u.getFullName()) + "\","
             + "\"address\":\"" + esc(u.getAddress()) + "\","
             + "\"role\":\"" + u.getRole() + "\"}";
    }

    private String usersToJson(List<User> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) { if (i > 0) sb.append(","); sb.append(userToJson(list.get(i))); }
        return sb.append("]").toString();
    }

    private String couponsToJson(List<Coupon> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Coupon c = list.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"code\":\"").append(esc(c.getCode())).append("\",")
              .append("\"discountPercent\":").append(c.getDiscountPercent()).append(",")
              .append("\"active\":").append(c.isActive()).append("}");
        }
        return sb.append("]").toString();
    }

    // ── Utilities ─────────────────────────────────────────────────────────

    private void sendJson(HttpExchange ex, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    private String readBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) { return new String(is.readAllBytes(), StandardCharsets.UTF_8); }
    }

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
        int end = start;
        while (end < json.length() && ",}]".indexOf(json.charAt(end)) < 0) end++;
        return json.substring(start, end).trim();
    }

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

    private List<OrderItem> parseOrderItems(String itemsJson, String restaurantId) {
        List<OrderItem> result = new ArrayList<>();
        try {
            Restaurant restaurant = restaurantService.getRestaurant(restaurantId);
            String inner = itemsJson.trim();
            if (inner.startsWith("[")) inner = inner.substring(1);
            if (inner.endsWith("]")) inner = inner.substring(0, inner.length() - 1);
            for (String obj : inner.split("\\},\\s*\\{")) {
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
    private String nvl(String s, String def) { return s != null ? s : def; }
    private int parseInt(String s, int def) { try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; } }
    private double parseDouble(String s, double def) { try { return Double.parseDouble(s.trim()); } catch (Exception e) { return def; } }
}
