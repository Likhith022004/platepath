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
public class CustomerPreferenceResponse {
    private List<String> topCuisines;
    private List<String> frequentItems;
    private String preferredOrderTime;
    private Double averageOrderValue;
    private String orderFrequency;
    private String personalityType;
}
