package com.fooddelivery.service.impl;

import com.fooddelivery.dto.request.UpdateStatusRequest;
import com.fooddelivery.dto.response.TrackingUpdateResponse;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.TrackingUpdate;
import com.fooddelivery.entity.User;
import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.enums.UserRole;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.exception.UnauthorizedDeliveryUpdateException;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.TrackingRepository;
import com.fooddelivery.repository.UserRepository;
import com.fooddelivery.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TrackingServiceImpl implements TrackingService {

    private final TrackingRepository    trackingRepository;
    private final OrderRepository       orderRepository;
    private final UserRepository        userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Valid forward-only status transitions for delivery boys.
     * Admin-driven transitions (PLACED→CONFIRMED, CONFIRMED→PREPARING)
     * are handled in OrderService, not here.
     */
    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS =
            new EnumMap<>(OrderStatus.class);

    static {
        VALID_TRANSITIONS.put(OrderStatus.CONFIRMED,
                EnumSet.of(OrderStatus.PREPARING));
        VALID_TRANSITIONS.put(OrderStatus.PREPARING,
                EnumSet.of(OrderStatus.OUT_FOR_DELIVERY));
        VALID_TRANSITIONS.put(OrderStatus.OUT_FOR_DELIVERY,
                EnumSet.of(OrderStatus.DELIVERED));
    }

    @Override
    public TrackingUpdateResponse updateOrderStatus(
            Long orderId, UpdateStatusRequest dto, String deliveryBoyEmail) {

        Order order = findOrder(orderId);
        User deliveryBoy = findUserByEmail(deliveryBoyEmail);

        // Only the assigned delivery boy may push status updates
        if (order.getDeliveryBoy() == null
                || !order.getDeliveryBoy().getId().equals(deliveryBoy.getId())) {
            throw new UnauthorizedDeliveryUpdateException(deliveryBoy.getId(), orderId);
        }

        // Validate forward-only transition
        Set<OrderStatus> allowed = VALID_TRANSITIONS.getOrDefault(
                order.getStatus(), EnumSet.noneOf(OrderStatus.class));

        if (!allowed.contains(dto.getStatus())) {
            throw new IllegalStateException(String.format(
                    "Invalid status transition: %s → %s. Allowed next states: %s",
                    order.getStatus(), dto.getStatus(), allowed));
        }

        // Persist tracking record
        TrackingUpdate update = TrackingUpdate.builder()
                .order(order)
                .status(dto.getStatus())
                .message(dto.getMessage() != null
                        ? dto.getMessage()
                        : defaultMessage(dto.getStatus()))
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .build();

        TrackingUpdate saved = trackingRepository.save(Objects.requireNonNull(update));

        // Update order status
        order.setStatus(dto.getStatus());
        orderRepository.save(order);

        log.info("Order #{} status updated to {} by delivery boy '{}'",
                orderId, dto.getStatus(), deliveryBoyEmail);

        TrackingUpdateResponse response = toResponse(saved, orderId);

        // Push to WebSocket topic so all listeners (customer, admin) get live updates
        messagingTemplate.convertAndSend(
                "/topic/order/" + orderId + "/tracking",
                Objects.requireNonNull(response));

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackingUpdateResponse> getTrackingHistory(Long orderId, String requesterEmail) {
        Order order = findOrder(orderId);
        User requester = findUserByEmail(requesterEmail);

        // Customers may only view their own orders' history
        if (requester.getRole() == UserRole.CUSTOMER
                && !order.getCustomer().getId().equals(requester.getId())) {
            throw new UnauthorizedDeliveryUpdateException(
                    "You can only view tracking for your own orders");
        }

        return trackingRepository.findByOrder_IdOrderByTimestampAsc(orderId).stream()
                .map(t -> toResponse(t, orderId))
                .collect(Collectors.toList());
    }

    // ===== Helpers =====

    private TrackingUpdateResponse toResponse(TrackingUpdate t, Long orderId) {
        return TrackingUpdateResponse.builder()
                .id(t.getId())
                .orderId(orderId)
                .status(t.getStatus())
                .message(t.getMessage())
                .latitude(t.getLatitude())
                .longitude(t.getLongitude())
                .timestamp(t.getTimestamp())
                .build();
    }

    private String defaultMessage(OrderStatus status) {
        return switch (status) {
            case PREPARING       -> "Restaurant is preparing your order!";
            case OUT_FOR_DELIVERY -> "Your order is on the way!";
            case DELIVERED       -> "Your order has been delivered. Enjoy your meal!";
            default              -> status.name();
        };
    }

    private Order findOrder(Long id) {
        return orderRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}
