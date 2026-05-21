package com.fooddelivery.dto.request;

import com.fooddelivery.enums.CuisineType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RestaurantRequest {

    @NotBlank(message = "Restaurant name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Cuisine type is required")
    private CuisineType cuisineType;

    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private Integer avgPrepTimeMinutes = 30;
}
