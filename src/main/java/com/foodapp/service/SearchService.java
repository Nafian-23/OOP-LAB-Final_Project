package com.foodapp.service;

import com.foodapp.model.MenuItem;
import com.foodapp.model.Restaurant;
import com.foodapp.repository.RestaurantRepositoryDB;

import java.util.List;
import java.util.stream.Collectors;

public class SearchService {
    private final RestaurantRepositoryDB restaurantRepository;

    public SearchService(RestaurantRepositoryDB restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    public List<Restaurant> searchRestaurants(String query) {
        if (query == null || query.isBlank()) return restaurantRepository.findAll();
        String lower = query.toLowerCase();
        return restaurantRepository.findAll().stream()
                .filter(r -> r.getName().toLowerCase().contains(lower)
                        || r.getCuisineType().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    public List<MenuItem> searchMenuItems(String query) {
        if (query == null || query.isBlank()) {
            return restaurantRepository.findAll().stream()
                    .flatMap(r -> r.getMenuItems().stream()).collect(Collectors.toList());
        }
        String lower = query.toLowerCase();
        return restaurantRepository.findAll().stream()
                .flatMap(r -> r.getMenuItems().stream())
                .filter(i -> i.getName().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    public List<MenuItem> filterAvailableItems(List<MenuItem> items) {
        return items.stream().filter(MenuItem::isAvailable).collect(Collectors.toList());
    }
}
