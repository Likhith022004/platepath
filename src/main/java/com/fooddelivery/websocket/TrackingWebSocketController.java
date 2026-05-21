package com.fooddelivery.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

/**
 * Handles incoming STOMP messages from the delivery boy client
 * and broadcasts location updates to all order subscribers.
 *
 * Flow: delivery boy JS → STOMP /app/order/{id}/location
 *       → broadcast to /topic/order/{id}/tracking
 *       → customer map marker updates live
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class TrackingWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Receives a location ping from the delivery boy and re-broadcasts it.
     * The @SendTo annotation handles the fan-out automatically.
     */
    @MessageMapping("/order/{orderId}/location")
    @SendTo("/topic/order/{orderId}/tracking")
    public LocationUpdateMessage handleLocationUpdate(
            @DestinationVariable Long orderId,
            LocationUpdateMessage message) {

        log.debug("Location update for order #{}: lat={}, lng={}",
                orderId, message.getLatitude(), message.getLongitude());

        // Stamp the server-side timestamp before broadcasting
        message.setOrderId(orderId);
        message.setTimestamp(LocalDateTime.now().toString());
        return message;
    }

    /**
     * Allows services to push arbitrary order events to a specific order channel.
     * Called programmatically from OrderServiceImpl / TrackingServiceImpl.
     */
    public void pushToOrder(Long orderId, Object payload) {
        messagingTemplate.convertAndSend("/topic/order/" + orderId + "/status",
                java.util.Objects.requireNonNull(payload));
    }
}
