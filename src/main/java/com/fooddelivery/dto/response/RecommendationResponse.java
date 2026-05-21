package com.fooddelivery.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecommendationResponse {
    private String itemName;
    private String restaurantName;
    private String reason;
    private String cuisineType;
    private Double estimatedPrice;
}
