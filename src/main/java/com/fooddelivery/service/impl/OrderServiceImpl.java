package com.fooddelivery.service.impl;

import com.fooddelivery.dto.request.OrderItemRequest;
import com.fooddelivery.dto.request.PlaceOrderRequest;
import com.fooddelivery.dto.response.OrderResponse;
import com.fooddelivery.entity.*;
import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.enums.UserRole;
import com.fooddelivery.exception.*;
import com.fooddelivery.repository.*;
import com.fooddelivery.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository       orderRepository;
    private final UserRepository        userRepository;
    private final RestaurantRepository  restaurantRepository;
    private final MenuItemRepository    menuItemRepository;
    private final TrackingRepository    trackingRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final OrderMapper           orderMapper;

    // ===== Place Order =====

    @Override
    public OrderResponse placeOrder(PlaceOrderRequest dto, String customerEmail) {
        User customer = findUserByEmail(customerEmail);
        Restaurant restaurant = findRestaurant(dto.getRestaurantId());

        if (!restaurant.getIsOpen()) {
            throw new RestaurantClosedException(restaurant.getName());
        }

        // Build the Order shell first so items can reference it
        Order order = Order.builder()
                .customer(customer)
                .restaurant(restaurant)
                .deliveryAddress(dto.getDeliveryAddress())
                .deliveryLatitude(dto.getDeliveryLatitude())
                .deliveryLongitude(dto.getDeliveryLongitude())
                .specialInstructions(dto.getSpecialInstructions())
                .status(OrderStatus.PLACED)
                .build();

        // Process line items — snapshot unit prices at order time
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest req : dto.getItems()) {
            MenuItem menuItem = findMenuItem(req.getMenuItemId());

            if (!menuItem.getIsAvailable()) {
                throw new RuntimeException(
                        "Menu item '" + menuItem.getName() + "' is currently unavailable");
            }
            if (!menuItem.getRestaurant().getId().equals(restaurant.getId())) {
                throw new RuntimeException(
                        "Item '" + menuItem.getName() + "' does not belong to the selected restaurant");
            }

            OrderItem lineItem = OrderItem.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .quantity(req.getQuantity())
                    .unitPrice(menuItem.getPrice())
                    .build();
            order.getItems().add(lineItem);
            total = total.add(menuItem.getPrice().multiply(BigDecimal.valueOf(req.getQuantity())));
        }

        int estimatedMinutes = restaurant.getAvgPrepTimeMinutes() + 20;
        order.setTotalAmount(total);
        order.setEstimatedMinutes(estimatedMinutes);
        order.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(estimatedMinutes));

        Order saved = orderRepository.save(order);

        // Initial tracking record
        saveTracking(saved, OrderStatus.PLACED, "Your order has been placed successfully!", null, null);

        // Notify all subscribers (customer polling the tracking page)
        pushStatus(saved.getId(), OrderStatus.PLACED, "Order placed successfully");

        log.info("Order #{} placed by customer '{}' at restaurant '{}'",
                saved.getId(), customerEmail, restaurant.getName());
        return orderMapper.toOrderResponse(saved);
    }

    // ===== Cancel Order =====

    @Override
    public OrderResponse cancelOrder(Long orderId, String customerEmail) {
        Order order = findOrder(orderId);
        User customer = findUserByEmail(customerEmail);

        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new UnauthorizedDeliveryUpdateException(
                    "You can only cancel your own orders");
        }
        if (order.getStatus() == OrderStatus.OUT_FOR_DELIVERY
                || order.getStatus() == OrderStatus.DELIVERED) {
            throw new OrderAlreadyCancelledException(
                    "Order #" + orderId + " cannot be cancelled — it is already " + order.getStatus());
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderAlreadyCancelledException(orderId);
        }

        order.setStatus(OrderStatus.CANCELLED);
        saveTracking(order, OrderStatus.CANCELLED, "Order cancelled by customer", null, null);
        pushStatus(orderId, OrderStatus.CANCELLED, "Order has been cancelled");

        log.info("Order #{} cancelled by '{}'", orderId, customerEmail);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    // ===== Query Methods =====

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(String customerEmail, Pageable pageable) {
        User customer = findUserByEmail(customerEmail);
        return orderRepository.findByCustomer_Id(customer.getId(), pageable)
                .map(orderMapper::toOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, String requesterEmail) {
        Order order = findOrder(orderId);
        User requester = findUserByEmail(requesterEmail);

        boolean isOwner = order.getCustomer().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == UserRole.ADMIN;
        boolean isAssignedBoy = requester.getRole() == UserRole.DELIVERY_BOY
                && order.getDeliveryBoy() != null
                && order.getDeliveryBoy().getId().equals(requester.getId());
        boolean isRestaurantOwner = requester.getRole() == UserRole.RESTAURANT_OWNER
                && order.getRestaurant().getOwner().getId().equals(requester.getId());

        if (!isOwner && !isAdmin && !isAssignedBoy && !isRestaurantOwner) {
            throw new UnauthorizedDeliveryUpdateException(
                    "You do not have permission to view order #" + orderId);
        }
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersForRestaurant(Long restaurantId, String ownerEmail, Pageable pageable) {
        Restaurant restaurant = findRestaurant(restaurantId);
        User owner = findUserByEmail(ownerEmail);

        if (!restaurant.getOwner().getId().equals(owner.getId())
                && owner.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedDeliveryUpdateException(
                    "You do not own restaurant #" + restaurantId);
        }
        return orderRepository.findByRestaurant_Id(restaurantId, pageable)
                .map(orderMapper::toOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrdersAdmin(OrderStatus status, Pageable pageable) {
        var pg = Objects.requireNonNull(pageable);
        if (status != null) {
            return orderRepository.findByStatus(status, pg)
                    .map(orderMapper::toOrderResponse);
        }
        return orderRepository.findAll(pg).map(orderMapper::toOrderResponse);
    }

    // ===== Admin Actions =====

    @Override
    public OrderResponse confirmOrder(Long orderId, String adminEmail) {
        Order order = findOrder(orderId);
        if (order.getStatus() != OrderStatus.PLACED) {
            throw new IllegalStateException(
                    "Only PLACED orders can be confirmed. Current status: " + order.getStatus());
        }
        order.setStatus(OrderStatus.CONFIRMED);
        saveTracking(order, OrderStatus.CONFIRMED, "Your order has been confirmed!", null, null);
        pushStatus(orderId, OrderStatus.CONFIRMED, "Order confirmed by restaurant");

        log.info("Order #{} confirmed by admin '{}'", orderId, adminEmail);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse assignDeliveryBoy(Long orderId, Long deliveryBoyId, String adminEmail) {
        Order order = findOrder(orderId);
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Delivery boy can only be assigned to CONFIRMED orders. Current: " + order.getStatus());
        }

        User deliveryBoy = userRepository.findById(Objects.requireNonNull(deliveryBoyId))
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", deliveryBoyId));
        if (deliveryBoy.getRole() != UserRole.DELIVERY_BOY) {
            throw new RuntimeException("User #" + deliveryBoyId + " is not a DELIVERY_BOY");
        }

        order.setDeliveryBoy(deliveryBoy);
        order.setStatus(OrderStatus.PREPARING);
        saveTracking(order, OrderStatus.PREPARING,
                "Delivery partner " + deliveryBoy.getName() + " has been assigned!", null, null);
        pushStatus(orderId, OrderStatus.PREPARING,
                "Delivery boy assigned: " + deliveryBoy.getName());

        log.info("Delivery boy '{}' assigned to order #{} by admin '{}'",
                deliveryBoy.getName(), orderId, adminEmail);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    // ===== Private Helpers =====

    private Order findOrder(Long id) {
        return orderRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private Restaurant findRestaurant(Long id) {
        return restaurantRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));
    }

    private MenuItem findMenuItem(Long id) {
        return menuItemRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));
    }

    private void saveTracking(Order order, OrderStatus status, String message,
                              Double lat, Double lng) {
        TrackingUpdate update = TrackingUpdate.builder()
                .order(order)
                .status(status)
                .message(message)
                .latitude(lat)
                .longitude(lng)
                .build();
        trackingRepository.save(Objects.requireNonNull(update));
    }

    private void pushStatus(Long orderId, OrderStatus status, String message) {
        Map<String, Object> payload = Map.of(
                "orderId", orderId,
                "status", status.name(),
                "message", message);
        messagingTemplate.convertAndSend("/topic/order/" + orderId + "/status",
                Objects.requireNonNull(payload));
    }
}
