package com.fooddelivery.service.impl;

import com.fooddelivery.dto.response.OrderItemResponse;
import com.fooddelivery.dto.response.OrderResponse;
import com.fooddelivery.dto.response.TrackingUpdateResponse;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.TrackingUpdate;
import com.fooddelivery.repository.TrackingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Shared mapper for Order → OrderResponse.
 * Centralised here to avoid duplicating lazy-load logic across services.
 */
@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final TrackingRepository trackingRepository;

    public OrderResponse toOrderResponse(Order order) {
        List<TrackingUpdate> updates =
                trackingRepository.findByOrder_IdOrderByTimestampAsc(order.getId());

        List<OrderItemResponse> items = order.getItems().stream()
                .map(oi -> OrderItemResponse.builder()
                        .menuItemId(oi.getMenuItem().getId())
                        .itemName(oi.getMenuItem().getName())
                        .quantity(oi.getQuantity())
                        .unitPrice(oi.getUnitPrice())
                        .subtotal(oi.getUnitPrice()
                                .multiply(BigDecimal.valueOf(oi.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        List<TrackingUpdateResponse> trackingResponses = updates.stream()
                .map(t -> TrackingUpdateResponse.builder()
                        .id(t.getId())
                        .orderId(order.getId())
                        .status(t.getStatus())
                        .message(t.getMessage())
                        .latitude(t.getLatitude())
                        .longitude(t.getLongitude())
                        .timestamp(t.getTimestamp())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getName())
                .customerPhone(order.getCustomer().getPhone())
                .restaurantId(order.getRestaurant().getId())
                .restaurantName(order.getRestaurant().getName())
                .restaurantAddress(order.getRestaurant().getAddress())
                .items(items)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .deliveryAddress(order.getDeliveryAddress())
                .deliveryBoyId(order.getDeliveryBoy() != null ? order.getDeliveryBoy().getId() : null)
                .deliveryBoyName(order.getDeliveryBoy() != null ? order.getDeliveryBoy().getName() : null)
                .deliveryBoyPhone(order.getDeliveryBoy() != null ? order.getDeliveryBoy().getPhone() : null)
                .estimatedDeliveryTime(order.getEstimatedDeliveryTime())
                .estimatedMinutes(order.getEstimatedMinutes())
                .specialInstructions(order.getSpecialInstructions())
                .placedAt(order.getPlacedAt())
                .updatedAt(order.getUpdatedAt())
                .trackingUpdates(trackingResponses)
                .build();
    }
}
