package com.fooddelivery.controller;

import com.fooddelivery.dto.request.UpdateStatusRequest;
import com.fooddelivery.dto.response.TrackingUpdateResponse;
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
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tracking", description = "Real-time order tracking endpoints")
public class TrackingController {

    private final TrackingService trackingService;

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('DELIVERY_BOY')")
    @Operation(summary = "Delivery boy updates order status + GPS location")
    public ResponseEntity<TrackingUpdateResponse> updateStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateStatusRequest dto,
            @AuthenticationPrincipal UserDetails principal) {
        TrackingUpdateResponse response =
                trackingService.updateOrderStatus(orderId, dto, principal.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}/history")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get full tracking history for an order")
    public ResponseEntity<List<TrackingUpdateResponse>> getHistory(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                trackingService.getTrackingHistory(orderId, principal.getUsername()));
    }
}
