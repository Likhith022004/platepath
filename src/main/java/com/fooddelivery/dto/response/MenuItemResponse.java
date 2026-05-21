package com.fooddelivery.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MenuItemResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private Boolean isAvailable;
    private Boolean isVegetarian;
    private String imageUrl;
    private Long restaurantId;
    private String restaurantName;
}
