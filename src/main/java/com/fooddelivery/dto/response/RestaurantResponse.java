package com.fooddelivery.dto.response;

import com.fooddelivery.enums.CuisineType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RestaurantResponse {

    private Long id;
    private String name;
    private String address;
    private CuisineType cuisineType;
    private Double rating;
    private Boolean isOpen;
    private Long ownerId;
    private String ownerName;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private Integer avgPrepTimeMinutes;
    private int menuItemCount;
    private Double deliveryRadiusKm;
    private LocalDateTime createdAt;
}
