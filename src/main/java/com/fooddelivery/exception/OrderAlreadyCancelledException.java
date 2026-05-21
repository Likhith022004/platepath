package com.fooddelivery.exception;

public class OrderAlreadyCancelledException extends RuntimeException {

    public OrderAlreadyCancelledException(Long orderId) {
        super("Order #" + orderId + " has already been cancelled");
    }

    public OrderAlreadyCancelledException(String message) {
        super(message);
    }
}
