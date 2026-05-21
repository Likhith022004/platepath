package com.fooddelivery.controller;

import com.fooddelivery.dto.request.UpdateStatusRequest;
import com.fooddelivery.dto.response.OrderResponse;
import com.fooddelivery.dto.response.TrackingUpdateResponse;
import com.fooddelivery.service.DeliveryService;
import com.fooddelivery.service.TrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('DELIVERY_BOY')")
@Tag(name = "Delivery Panel", description = "Delivery boy dashboard endpoints")
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final TrackingService trackingService;

    @GetMapping("/my-orders")
    @Operation(summary = "Get all orders assigned to the logged-in delivery boy")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(deliveryService.getMyAssignedOrders(principal.getUsername()));
    }

    @GetMapping("/active-order")
    @Operation(summary = "Get the current active (in-progress) order")
    public ResponseEntity<OrderResponse> getActiveOrder(
            @AuthenticationPrincipal UserDetails principal) {
        return deliveryService.getCurrentActiveOrder(principal.getUsername())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PutMapping("/{orderId}/update")
    @Operation(summary = "Update order status with GPS coordinates (delegates to TrackingService)")
    public ResponseEntity<TrackingUpdateResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateStatusRequest dto,
            @AuthenticationPrincipal UserDetails principal) {
        TrackingUpdateResponse response =
                trackingService.updateOrderStatus(orderId, dto, principal.getUsername());
        return ResponseEntity.ok(response);
    }
}
