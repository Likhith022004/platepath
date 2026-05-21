package com.fooddelivery.service;

import com.fooddelivery.dto.request.RestaurantRequest;
import com.fooddelivery.dto.response.MenuItemResponse;
import com.fooddelivery.dto.response.RestaurantResponse;
import com.fooddelivery.enums.CuisineType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RestaurantService {

    Page<RestaurantResponse> getAllRestaurants(String keyword, CuisineType cuisine, Pageable pageable);

    RestaurantResponse getRestaurantById(Long id);

    List<MenuItemResponse> getMenuByRestaurantId(Long id);

    RestaurantResponse createRestaurant(RestaurantRequest dto, String ownerEmail);

    RestaurantResponse updateRestaurant(Long id, RestaurantRequest dto, String ownerEmail);

    RestaurantResponse toggleRestaurantOpen(Long id, String ownerEmail);
}
