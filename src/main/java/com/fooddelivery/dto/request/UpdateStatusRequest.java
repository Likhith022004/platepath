package com.fooddelivery.dto.request;

import com.fooddelivery.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    private OrderStatus status;

    private Double latitude;
    private Double longitude;
    private String message;
}
