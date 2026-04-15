package com.foodapp.service;

import com.foodapp.model.MenuItem;
import com.foodapp.model.Restaurant;
import com.foodapp.repository.RestaurantRepositoryDB;

import java.util.*;
import java.util.stream.Collectors;

public class RestaurantService {
    private final RestaurantRepositoryDB restaurantRepository;

    public RestaurantService(RestaurantRepositoryDB restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    public Restaurant register(String name, String address, String cuisineType, String contact,
                               int openingHour, int closingHour, double deliveryRadiusKm, String area) {
        boolean exists = restaurantRepository.findAll().stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase(name));
        if (exists) throw new IllegalArgumentException("Restaurant name already exists: " + name);
        String id = UUID.randomUUID().toString();
        Restaurant r = new Restaurant(id, name, address, cuisineType, contact,
                openingHour, closingHour, deliveryRadiusKm, 0.0, 30,
                area != null ? area : address, new ArrayList<>());
        restaurantRepository.save(r);
        return r;
    }

    public List<Restaurant> findByArea(String address) {
        List<Restaurant> all = restaurantRepository.findAll();
        if (address == null || address.isBlank()) return all;
        String lower = address.toLowerCase();
        String[] words = lower.split("\\s+");
        List<Restaurant> matched = all.stream().filter(r -> {
            String rAddr = r.getAddress().toLowerCase();
            String rArea = r.getArea() != null ? r.getArea().toLowerCase() : "";
            for (String w : words) if (!w.isBlank() && (rAddr.contains(w) || rArea.contains(w))) return true;
            return false;
        }).collect(Collectors.toList());
        return matched.isEmpty() ? all : matched;
    }

    public List<Restaurant> sortByRating() {
        return restaurantRepository.findAll().stream()
                .sorted(Comparator.comparingDouble(Restaurant::getAverageRating).reversed())
                .collect(Collectors.toList());
    }

    public List<Restaurant> sortByDeliveryTime() {
        return restaurantRepository.findAll().stream()
                .sorted(Comparator.comparingInt(Restaurant::getEstimatedDeliveryMinutes))
                .collect(Collectors.toList());
    }

    public List<Restaurant> sortByName() {
        return restaurantRepository.findAll().stream()
                .sorted(Comparator.comparing(Restaurant::getName))
                .collect(Collectors.toList());
    }

    public MenuItem addMenuItem(String restaurantId, String name, double price, String description,
                                int stockQuantity, boolean available, List<String> customizationOptions) {
        Restaurant restaurant = getRestaurant(restaurantId);
        String itemId = UUID.randomUUID().toString();
        MenuItem item = new MenuItem(itemId, restaurantId, name, price, description,
                available, stockQuantity, customizationOptions != null ? customizationOptions : new ArrayList<>());
        restaurant.getMenuItems().add(item);
        restaurantRepository.update(restaurant);
        return item;
    }

    public void updateMenuItemAvailability(String restaurantId, String itemId, boolean available) {
        Restaurant restaurant = getRestaurant(restaurantId);
        restaurant.getMenuItems().stream().filter(i -> i.getId().equals(itemId))
                .findFirst().ifPresent(i -> i.setAvailable(available));
        restaurantRepository.update(restaurant);
    }

    public void updateMenuItemStock(String restaurantId, String itemId, int qty) {
        Restaurant restaurant = getRestaurant(restaurantId);
        restaurant.getMenuItems().stream().filter(i -> i.getId().equals(itemId))
                .findFirst().ifPresent(i -> i.setStockQuantity(qty));
        restaurantRepository.update(restaurant);
    }

    public Restaurant getRestaurant(String id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found: " + id));
    }

    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    public void deleteRestaurant(String id) {
        restaurantRepository.deleteById(id);
    }
}
