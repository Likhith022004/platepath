package com.fooddelivery.service;

import com.fooddelivery.dto.response.OrderResponse;

import java.util.List;
import java.util.Optional;

public interface DeliveryService {

    List<OrderResponse> getMyAssignedOrders(String deliveryBoyEmail);

    Optional<OrderResponse> getCurrentActiveOrder(String deliveryBoyEmail);
}
