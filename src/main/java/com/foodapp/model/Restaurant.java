package com.foodapp.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Restaurant implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String address;
    private String cuisineType;
    private String contact;
    private int openingHour;
    private int closingHour;
    private double deliveryRadiusKm;
    private double averageRating;
    private int estimatedDeliveryMinutes;
    private String area;
    private List<MenuItem> menuItems;

    public Restaurant() {}

    public Restaurant(String id, String name, String address, String cuisineType, String contact,
                      int openingHour, int closingHour, double deliveryRadiusKm,
                      double averageRating, int estimatedDeliveryMinutes, String area, List<MenuItem> menuItems) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.cuisineType = cuisineType;
        this.contact = contact;
        this.openingHour = openingHour;
        this.closingHour = closingHour;
        this.deliveryRadiusKm = deliveryRadiusKm;
        this.averageRating = averageRating;
        this.estimatedDeliveryMinutes = estimatedDeliveryMinutes;
        this.area = area;
        this.menuItems = menuItems;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCuisineType() { return cuisineType; }
    public void setCuisineType(String cuisineType) { this.cuisineType = cuisineType; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public int getOpeningHour() { return openingHour; }
    public void setOpeningHour(int openingHour) { this.openingHour = openingHour; }

    public int getClosingHour() { return closingHour; }
    public void setClosingHour(int closingHour) { this.closingHour = closingHour; }

    public double getDeliveryRadiusKm() { return deliveryRadiusKm; }
    public void setDeliveryRadiusKm(double deliveryRadiusKm) { this.deliveryRadiusKm = deliveryRadiusKm; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public int getEstimatedDeliveryMinutes() { return estimatedDeliveryMinutes; }
    public void setEstimatedDeliveryMinutes(int estimatedDeliveryMinutes) { this.estimatedDeliveryMinutes = estimatedDeliveryMinutes; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public List<MenuItem> getMenuItems() { return menuItems; }
    public void setMenuItems(List<MenuItem> menuItems) { this.menuItems = menuItems; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Restaurant)) return false;
        Restaurant r = (Restaurant) o;
        return Objects.equals(id, r.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Restaurant{id='" + id + "', name='" + name + "', cuisine='" + cuisineType + "', rating=" + averageRating + ", deliveryMin=" + estimatedDeliveryMinutes + "}";
    }
}
