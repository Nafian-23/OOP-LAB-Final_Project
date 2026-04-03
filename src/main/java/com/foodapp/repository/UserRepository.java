package com.foodapp.repository;

import com.foodapp.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {
    private static final String FILE_PATH = "users.dat";
    private final DataStorage dataStorage;
    private List<User> users;

    @SuppressWarnings("unchecked")
    public UserRepository(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
        Object loaded = dataStorage.load(FILE_PATH);
        this.users = (loaded instanceof List) ? (List<User>) loaded : new ArrayList<>();
    }

    public void save(User user) {
        users.removeIf(u -> u.getId().equals(user.getId()));
        users.add(user);
        dataStorage.save(users, FILE_PATH);
    }

    public Optional<User> findByUsername(String username) {
        return users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public List<User> findAll() {
        return new ArrayList<>(users);
    }
}
