package com.fooddelivery.controller;

import com.fooddelivery.dto.request.PlaceOrderRequest;
import com.fooddelivery.dto.response.OrderResponse;
import com.fooddelivery.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "Place and manage food orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Place a new order")
    public ResponseEntity<OrderResponse> placeOrder(
            @Valid @RequestBody PlaceOrderRequest dto,
            @AuthenticationPrincipal UserDetails principal) {
        OrderResponse response = orderService.placeOrder(dto, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get my order history (paginated)")
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal UserDetails principal,
            @PageableDefault(size = 10, sort = "placedAt") Pageable pageable) {
        return ResponseEntity.ok(orderService.getMyOrders(principal.getUsername(), pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get order detail by ID")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(orderService.getOrderById(id, principal.getUsername()));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(orderService.cancelOrder(id, principal.getUsername()));
    }

    @PutMapping("/{id}/assign-delivery")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign a delivery boy to an order (Admin)")
    public ResponseEntity<OrderResponse> assignDelivery(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body,
            @AuthenticationPrincipal UserDetails principal) {
        Long deliveryBoyId = body.get("deliveryBoyId");
        if (deliveryBoyId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(
                orderService.assignDeliveryBoy(id, deliveryBoyId, principal.getUsername()));
    }

    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Get orders for a specific restaurant")
    public ResponseEntity<Page<OrderResponse>> getRestaurantOrders(
            @PathVariable Long restaurantId,
            @AuthenticationPrincipal UserDetails principal,
            @PageableDefault(size = 20, sort = "placedAt") Pageable pageable) {
        return ResponseEntity.ok(
                orderService.getOrdersForRestaurant(restaurantId, principal.getUsername(), pageable));
    }
}
