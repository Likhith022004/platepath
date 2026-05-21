package com.fooddelivery.websocket;

import com.fooddelivery.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Client → server STOMP message payload for live GPS location updates.
 * Sent by the delivery boy's browser to /app/order/{orderId}/location
 * and broadcast to /topic/order/{orderId}/tracking.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdateMessage {

    private Long orderId;
    private Double latitude;
    private Double longitude;
    private OrderStatus status;
    private String message;
    private String timestamp;
}
