package com.foodapp.repository;

import com.foodapp.model.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OrderRepository {
    private static final String FILE_PATH = "orders.dat";
    private final DataStorage dataStorage;
    private List<Order> orders;

    @SuppressWarnings("unchecked")
    public OrderRepository(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
        Object loaded = dataStorage.load(FILE_PATH);
        this.orders = (loaded instanceof List) ? (List<Order>) loaded : new ArrayList<>();
    }

    public void save(Order order) {
        orders.removeIf(o -> o.getId().equals(order.getId()));
        orders.add(order);
        dataStorage.save(orders, FILE_PATH);
    }

    public Optional<Order> findById(String id) {
        return orders.stream()
                .filter(o -> o.getId().equals(id))
                .findFirst();
    }

    public List<Order> findByCustomerId(String customerId) {
        return orders.stream()
                .filter(o -> o.getCustomerId().equals(customerId))
                .collect(Collectors.toList());
    }

    public List<Order> findByRestaurantId(String restaurantId) {
        return orders.stream()
                .filter(o -> o.getRestaurantId().equals(restaurantId))
                .collect(Collectors.toList());
    }

    public void update(Order order) {
        orders.removeIf(o -> o.getId().equals(order.getId()));
        orders.add(order);
        dataStorage.save(orders, FILE_PATH);
    }

    public List<Order> findAll() {
        return new ArrayList<>(orders);
    }
}
