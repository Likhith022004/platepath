package com.fooddelivery.service;

import com.fooddelivery.dto.request.PlaceOrderRequest;
import com.fooddelivery.dto.response.OrderResponse;
import com.fooddelivery.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponse placeOrder(PlaceOrderRequest dto, String customerEmail);

    OrderResponse cancelOrder(Long orderId, String customerEmail);

    Page<OrderResponse> getMyOrders(String customerEmail, Pageable pageable);

    OrderResponse getOrderById(Long orderId, String requesterEmail);

    OrderResponse assignDeliveryBoy(Long orderId, Long deliveryBoyId, String adminEmail);

    Page<OrderResponse> getOrdersForRestaurant(Long restaurantId, String ownerEmail, Pageable pageable);

    Page<OrderResponse> getAllOrdersAdmin(OrderStatus status, Pageable pageable);

    OrderResponse confirmOrder(Long orderId, String adminEmail);
}
