package com.fooddelivery.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {

    private Long menuItemId;
    private String itemName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
