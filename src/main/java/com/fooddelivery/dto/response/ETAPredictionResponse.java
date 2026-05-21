package com.fooddelivery.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ETAPredictionResponse {
    private Integer estimatedMinutes;
    private String confidence;           // HIGH / MEDIUM / LOW
    private Map<String, Integer> breakdown; // prepTime, waitTime, deliveryTime
}
