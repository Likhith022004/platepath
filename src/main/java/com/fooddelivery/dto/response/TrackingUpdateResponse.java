package com.fooddelivery.dto.response;

import com.fooddelivery.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TrackingUpdateResponse {

    private Long id;
    private Long orderId;
    private OrderStatus status;
    private String message;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
}
