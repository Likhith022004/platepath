package com.fooddelivery.ai;

import com.fooddelivery.dto.response.*;

import java.util.List;

public interface AIService {

    List<RecommendationResponse> getRecommendations(Long customerId);

    ETAPredictionResponse predictETA(Long orderId);

    RouteOptimizationResponse optimizeRoute(Long deliveryBoyId);

    CustomerPreferenceResponse analyzeCustomerPreferences(Long customerId);

    void detectDelays();

    AnalyticsResponse generateAnalytics(String period);
}
