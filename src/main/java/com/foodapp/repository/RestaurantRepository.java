package com.foodapp.repository;

import com.foodapp.model.Restaurant;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RestaurantRepository {
    private static final String FILE_PATH = "restaurants.dat";
    private final DataStorage dataStorage;
    private List<Restaurant> restaurants;

    @SuppressWarnings("unchecked")
    public RestaurantRepository(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
        Object loaded = dataStorage.load(FILE_PATH);
        this.restaurants = (loaded instanceof List) ? (List<Restaurant>) loaded : new ArrayList<>();
    }

    public void save(Restaurant restaurant) {
        restaurants.removeIf(r -> r.getId().equals(restaurant.getId()));
        restaurants.add(restaurant);
        dataStorage.save(restaurants, FILE_PATH);
    }

    public List<Restaurant> findAll() {
        return new ArrayList<>(restaurants);
    }

    public Optional<Restaurant> findById(String id) {
        return restaurants.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst();
    }

    public void update(Restaurant restaurant) {
        restaurants.removeIf(r -> r.getId().equals(restaurant.getId()));
        restaurants.add(restaurant);
        dataStorage.save(restaurants, FILE_PATH);
    }
}
