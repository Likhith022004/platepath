package com.fooddelivery.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsResponse {

    // Raw aggregated fields (always populated)
    private String period;
    private LocalDateTime generatedAt;
    private long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal avgOrderValue;
    private Map<String, Long> ordersByStatus;

    // AI-generated fields (populated when Claude is available)
    private String summary;
    private List<String> keyInsights;
    private List<RestaurantStat> topRestaurants;
    private String peakHour;
    private String recommendation;
    private String revenueGrowthTrend;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RestaurantStat {
        private String name;
        private Long orders;
        private Double revenue;
    }
}
