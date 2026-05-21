package com.fooddelivery.exception;

public class RestaurantClosedException extends RuntimeException {

    public RestaurantClosedException(String restaurantName) {
        super("Restaurant '" + restaurantName + "' is currently closed and not accepting orders");
    }
}
