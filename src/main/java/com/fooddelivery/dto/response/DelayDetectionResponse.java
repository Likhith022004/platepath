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
public class DelayDetectionResponse {
    private Boolean isDelayed;
    private Integer delayMinutes;
    private String suggestedAction;
    private String customerMessage;
}
