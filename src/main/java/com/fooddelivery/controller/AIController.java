package com.fooddelivery.controller;

import com.fooddelivery.ai.AIService;
import com.fooddelivery.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @GetMapping("/recommendations")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ResponseEntity<List<RecommendationResponse>> getRecommendations(
            @RequestParam Long customerId) {
        return ResponseEntity.ok(aiService.getRecommendations(customerId));
    }

    @GetMapping("/eta/{orderId}")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ResponseEntity<ETAPredictionResponse> predictETA(@PathVariable Long orderId) {
        return ResponseEntity.ok(aiService.predictETA(orderId));
    }

    @GetMapping("/route-optimize")
    @PreAuthorize("hasAnyRole('DELIVERY_BOY','ADMIN')")
    public ResponseEntity<RouteOptimizationResponse> optimizeRoute(
            @RequestParam Long deliveryBoyId) {
        return ResponseEntity.ok(aiService.optimizeRoute(deliveryBoyId));
    }

    @GetMapping("/customer-analysis")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ResponseEntity<CustomerPreferenceResponse> analyzeCustomer(
            @RequestParam Long customerId) {
        return ResponseEntity.ok(aiService.analyzeCustomerPreferences(customerId));
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalyticsResponse> getAnalytics(
            @RequestParam(defaultValue = "WEEKLY") String period) {
        return ResponseEntity.ok(aiService.generateAnalytics(period));
    }
}
