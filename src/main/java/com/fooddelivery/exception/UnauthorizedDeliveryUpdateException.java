package com.fooddelivery.exception;

public class UnauthorizedDeliveryUpdateException extends RuntimeException {

    public UnauthorizedDeliveryUpdateException(Long deliveryBoyId, Long orderId) {
        super("Delivery boy #" + deliveryBoyId
                + " is not assigned to order #" + orderId
                + " and cannot update its status");
    }

    public UnauthorizedDeliveryUpdateException(String message) {
        super(message);
    }
}
