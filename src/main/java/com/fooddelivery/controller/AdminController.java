package com.fooddelivery.controller;

import com.fooddelivery.dto.response.OrderResponse;
import com.fooddelivery.dto.response.UserSummaryResponse;
import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.enums.UserRole;
import com.fooddelivery.service.OrderService;
import com.fooddelivery.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin-only operations")
public class AdminController {

    private final OrderService orderService;
    private final UserService  userService;

    // ===== Order Management =====

    @GetMapping("/orders")
    @Operation(summary = "Get all orders, optionally filtered by status")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20, sort = "placedAt") Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrdersAdmin(status, pageable));
    }

    @PutMapping("/orders/{id}/confirm")
    @Operation(summary = "Confirm a placed order")
    public ResponseEntity<OrderResponse> confirmOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(orderService.confirmOrder(id, principal.getUsername()));
    }

    @PutMapping("/orders/{id}/assign")
    @Operation(summary = "Assign a delivery boy to a confirmed order")
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

    // ===== User Management =====

    @GetMapping("/users")
    @Operation(summary = "List all registered users")
    public ResponseEntity<List<UserSummaryResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/delivery-boys")
    @Operation(summary = "List all delivery boys")
    public ResponseEntity<List<UserSummaryResponse>> getDeliveryBoys() {
        return ResponseEntity.ok(userService.getUsersByRole(UserRole.DELIVERY_BOY));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user details by ID")
    public ResponseEntity<UserSummaryResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
