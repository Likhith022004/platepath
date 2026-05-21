package com.fooddelivery.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteOptimizationResponse {

    private List<RouteStop> optimizedSequence;
    private Integer totalEstimatedMinutes;
    private Double totalDistanceKm;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RouteStop {
        private Integer order;
        private String address;
        private Integer estimatedMinutes;
    }
}
