package com.foodapp.repository;

public interface DataStorage {
    void save(Object data, String filePath);
    Object load(String filePath);
}
