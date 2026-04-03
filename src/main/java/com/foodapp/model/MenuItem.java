package com.foodapp.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class MenuItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String restaurantId;
    private String name;
    private double price;
    private String description;
    private boolean available;
    private int stockQuantity;
    private List<String> customizationOptions;

    public MenuItem() {}

    public MenuItem(String id, String restaurantId, String name, double price, String description,
                    boolean available, int stockQuantity, List<String> customizationOptions) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.name = name;
        this.price = price;
        this.description = description;
        this.available = available;
        this.stockQuantity = stockQuantity;
        this.customizationOptions = customizationOptions;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRestaurantId() { return restaurantId; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public List<String> getCustomizationOptions() { return customizationOptions; }
    public void setCustomizationOptions(List<String> customizationOptions) { this.customizationOptions = customizationOptions; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MenuItem)) return false;
        MenuItem item = (MenuItem) o;
        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "MenuItem{id='" + id + "', name='" + name + "', price=" + price + ", available=" + available + ", stock=" + stockQuantity + "}";
    }
}
