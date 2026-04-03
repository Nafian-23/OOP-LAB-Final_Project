package com.foodapp.repository;

import java.io.*;
import java.util.ArrayList;

public class FileDataStorage implements DataStorage {

    @Override
    public void save(Object data, String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(data);
        } catch (IOException e) {
            System.err.println("Failed to save data to " + filePath + ": " + e.getMessage());
            throw new RuntimeException("Failed to save data: " + e.getMessage(), e);
        }
    }

    @Override
    public Object load(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return ois.readObject();
        } catch (FileNotFoundException e) {
            return new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load data from " + filePath + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
