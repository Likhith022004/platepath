package com.fooddelivery.service;

import com.fooddelivery.dto.request.MenuItemRequest;
import com.fooddelivery.dto.response.MenuItemResponse;

import java.util.List;

public interface MenuService {

    MenuItemResponse addMenuItem(Long restaurantId, MenuItemRequest dto, String ownerEmail);

    MenuItemResponse updateMenuItem(Long itemId, MenuItemRequest dto, String ownerEmail);

    void deleteMenuItem(Long itemId, String ownerEmail);

    MenuItemResponse toggleItemAvailability(Long itemId, String ownerEmail);

    List<MenuItemResponse> getMenuByRestaurant(Long restaurantId);
}
