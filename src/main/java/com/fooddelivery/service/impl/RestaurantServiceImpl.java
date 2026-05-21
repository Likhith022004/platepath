package com.fooddelivery.service.impl;

import com.fooddelivery.dto.request.RestaurantRequest;
import com.fooddelivery.dto.response.MenuItemResponse;
import com.fooddelivery.dto.response.RestaurantResponse;
import com.fooddelivery.entity.MenuItem;
import com.fooddelivery.entity.Restaurant;
import com.fooddelivery.entity.User;
import com.fooddelivery.enums.CuisineType;
import com.fooddelivery.enums.UserRole;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.exception.UnauthorizedDeliveryUpdateException;
import com.fooddelivery.repository.MenuItemRepository;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.UserRepository;
import com.fooddelivery.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<RestaurantResponse> getAllRestaurants(String keyword, CuisineType cuisine, Pageable pageable) {
        return restaurantRepository.searchRestaurants(cuisine, keyword, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantById(Long id) {
        return toResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuByRestaurantId(Long id) {
        Restaurant restaurant = findById(id);
        return menuItemRepository.findByRestaurant_IdAndIsAvailableTrue(id).stream()
                .map(item -> toMenuItemResponse(item, restaurant))
                .collect(Collectors.toList());
    }

    @Override
    public RestaurantResponse createRestaurant(RestaurantRequest dto, String ownerEmail) {
        User owner = findUserByEmail(ownerEmail);
        Restaurant restaurant = Restaurant.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .cuisineType(dto.getCuisineType())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .imageUrl(dto.getImageUrl())
                .avgPrepTimeMinutes(dto.getAvgPrepTimeMinutes() != null ? dto.getAvgPrepTimeMinutes() : 30)
                .owner(owner)
                .build();
        Restaurant saved = java.util.Objects.requireNonNull(restaurantRepository.save(restaurant));
        log.info("Restaurant '{}' created by owner '{}'", saved.getName(), ownerEmail);
        return toResponse(saved);
    }

    @Override
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest dto, String ownerEmail) {
        Restaurant restaurant = findById(id);
        User requester = findUserByEmail(ownerEmail);

        verifyOwnerOrAdmin(restaurant, requester, id);

        restaurant.setName(dto.getName());
        restaurant.setAddress(dto.getAddress());
        restaurant.setCuisineType(dto.getCuisineType());
        restaurant.setLatitude(dto.getLatitude());
        restaurant.setLongitude(dto.getLongitude());
        restaurant.setImageUrl(dto.getImageUrl());
        if (dto.getAvgPrepTimeMinutes() != null) {
            restaurant.setAvgPrepTimeMinutes(dto.getAvgPrepTimeMinutes());
        }

        return toResponse(restaurantRepository.save(restaurant));
    }

    @Override
    public RestaurantResponse toggleRestaurantOpen(Long id, String ownerEmail) {
        Restaurant restaurant = findById(id);
        User requester = findUserByEmail(ownerEmail);

        verifyOwnerOrAdmin(restaurant, requester, id);

        restaurant.setIsOpen(!restaurant.getIsOpen());
        log.info("Restaurant '{}' is now {}", restaurant.getName(),
                restaurant.getIsOpen() ? "OPEN" : "CLOSED");
        return toResponse(restaurantRepository.save(restaurant));
    }

    // ===== Helpers =====

    private Restaurant findById(Long id) {
        return restaurantRepository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private void verifyOwnerOrAdmin(Restaurant restaurant, User requester, Long restaurantId) {
        boolean isOwner = restaurant.getOwner().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == UserRole.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new UnauthorizedDeliveryUpdateException(
                    "User '" + requester.getEmail() + "' is not authorised to modify restaurant #" + restaurantId);
        }
    }

    private RestaurantResponse toResponse(Restaurant r) {
        return RestaurantResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .address(r.getAddress())
                .cuisineType(r.getCuisineType())
                .rating(r.getRating())
                .isOpen(r.getIsOpen())
                .ownerId(r.getOwner().getId())
                .ownerName(r.getOwner().getName())
                .latitude(r.getLatitude())
                .longitude(r.getLongitude())
                .imageUrl(r.getImageUrl())
                .avgPrepTimeMinutes(r.getAvgPrepTimeMinutes())
                .menuItemCount(r.getMenuItems().size())
                .deliveryRadiusKm(r.getDeliveryRadiusKm())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private MenuItemResponse toMenuItemResponse(MenuItem item, Restaurant restaurant) {
        return MenuItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .category(item.getCategory())
                .isAvailable(item.getIsAvailable())
                .isVegetarian(item.getIsVegetarian())
                .imageUrl(item.getImageUrl())
                .restaurantId(restaurant.getId())
                .restaurantName(restaurant.getName())
                .build();
    }
}
