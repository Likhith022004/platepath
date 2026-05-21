package com.fooddelivery.service.impl;

import com.fooddelivery.dto.response.OrderResponse;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.User;
import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.UserRepository;
import com.fooddelivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DeliveryServiceImpl implements DeliveryService {

    private final OrderRepository orderRepository;
    private final UserRepository  userRepository;
    private final OrderMapper     orderMapper;

    @Override
    public List<OrderResponse> getMyAssignedOrders(String deliveryBoyEmail) {
        User deliveryBoy = findByEmail(deliveryBoyEmail);

        return orderRepository.findByDeliveryBoy_IdAndStatusIn(
                        deliveryBoy.getId(),
                        List.of(OrderStatus.PREPARING, OrderStatus.OUT_FOR_DELIVERY,
                                OrderStatus.CONFIRMED))
                .stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<OrderResponse> getCurrentActiveOrder(String deliveryBoyEmail) {
        User deliveryBoy = findByEmail(deliveryBoyEmail);

        // The "active" order is the one furthest along in the pipeline
        List<Order> active = orderRepository.findByDeliveryBoy_IdAndStatusIn(
                deliveryBoy.getId(),
                List.of(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.PREPARING));

        return active.stream()
                // Prefer OUT_FOR_DELIVERY over PREPARING
                .sorted((a, b) -> b.getStatus().ordinal() - a.getStatus().ordinal())
                .findFirst()
                .map(orderMapper::toOrderResponse);
    }

    // ===== Helper =====

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}
