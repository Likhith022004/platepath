package com.fooddelivery.dto.response;

import com.fooddelivery.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private Long restaurantId;
    private String restaurantName;
    private String restaurantAddress;
    private List<OrderItemResponse> items;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String deliveryAddress;
    private Long deliveryBoyId;
    private String deliveryBoyName;
    private String deliveryBoyPhone;
    private LocalDateTime estimatedDeliveryTime;
    private Integer estimatedMinutes;
    private String specialInstructions;
    private LocalDateTime placedAt;
    private LocalDateTime updatedAt;
    private List<TrackingUpdateResponse> trackingUpdates;
}
