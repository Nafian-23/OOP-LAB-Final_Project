package com.foodapp.api;

import com.foodapp.model.Restaurant;
import com.foodapp.repository.RestaurantRepositoryDB;
import com.foodapp.repository.DatabaseHelper;

import jakarta.jws.WebService;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import java.util.List;

@WebService(serviceName = "FoodAppService", portName = "FoodAppPort", targetNamespace = "http://foodapp.com/")
public class FoodAppWebService {

    private final RestaurantRepositoryDB restaurantRepo = new RestaurantRepositoryDB();

    static {
        DatabaseHelper.initializeDatabase();
    }

    @WebMethod(operationName = "getRestaurantsByArea")
    public List<Restaurant> getRestaurantsByArea(@WebParam(name = "area") String area) {
        return restaurantRepo.findByArea(area);
    }

    @WebMethod(operationName = "getAllRestaurants")
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepo.findAll();
    }

    // Add more methods as needed
}