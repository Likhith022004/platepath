package com.fooddelivery.service.impl;

import com.fooddelivery.dto.request.MenuItemRequest;
import com.fooddelivery.dto.response.MenuItemResponse;
import com.fooddelivery.entity.MenuItem;
import com.fooddelivery.entity.Restaurant;
import com.fooddelivery.entity.User;
import com.fooddelivery.enums.UserRole;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.exception.UnauthorizedDeliveryUpdateException;
import com.fooddelivery.repository.MenuItemRepository;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.UserRepository;
import com.fooddelivery.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MenuServiceImpl implements MenuService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    @Override
    public MenuItemResponse addMenuItem(Long restaurantId, MenuItemRequest dto, String ownerEmail) {
        Restaurant restaurant = findRestaurant(restaurantId);
        verifyOwner(restaurant, ownerEmail);

        MenuItem item = MenuItem.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .category(dto.getCategory())
                .isVegetarian(dto.getIsVegetarian() != null ? dto.getIsVegetarian() : false)
                .imageUrl(dto.getImageUrl())
                .restaurant(restaurant)
                .build();

        MenuItem saved = java.util.Objects.requireNonNull(menuItemRepository.save(item));
        log.info("Menu item '{}' added to restaurant '{}'", saved.getName(), restaurant.getName());
        return toResponse(saved);
    }

    @Override
    public MenuItemResponse updateMenuItem(Long itemId, MenuItemRequest dto, String ownerEmail) {
        MenuItem item = findItem(itemId);
        verifyOwner(item.getRestaurant(), ownerEmail);

        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setCategory(dto.getCategory());
        if (dto.getIsVegetarian() != null) item.setIsVegetarian(dto.getIsVegetarian());
        if (dto.getImageUrl() != null) item.setImageUrl(dto.getImageUrl());

        return toResponse(menuItemRepository.save(item));
    }

    @Override
    public void deleteMenuItem(Long itemId, String ownerEmail) {
        MenuItem item = findItem(itemId);
        verifyOwner(item.getRestaurant(), ownerEmail);
        menuItemRepository.delete(item);
        log.info("Menu item '{}' deleted", item.getName());
    }

    @Override
    public MenuItemResponse toggleItemAvailability(Long itemId, String ownerEmail) {
        MenuItem item = findItem(itemId);
        verifyOwner(item.getRestaurant(), ownerEmail);
        item.setIsAvailable(!item.getIsAvailable());
        return toResponse(menuItemRepository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurant_Id(restaurantId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ===== Helpers =====

    private Restaurant findRestaurant(Long id) {
        return restaurantRepository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));
    }

    private MenuItem findItem(Long id) {
        return menuItemRepository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));
    }

    private void verifyOwner(Restaurant restaurant, String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", requesterEmail));
        boolean isOwner = restaurant.getOwner().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == UserRole.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new UnauthorizedDeliveryUpdateException(
                    "User '" + requesterEmail + "' is not authorised to manage this restaurant's menu");
        }
    }

    private MenuItemResponse toResponse(MenuItem item) {
        return MenuItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .category(item.getCategory())
                .isAvailable(item.getIsAvailable())
                .isVegetarian(item.getIsVegetarian())
                .imageUrl(item.getImageUrl())
                .restaurantId(item.getRestaurant().getId())
                .restaurantName(item.getRestaurant().getName())
                .build();
    }
}
