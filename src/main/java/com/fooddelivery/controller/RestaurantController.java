package com.fooddelivery.controller;

import com.fooddelivery.dto.request.MenuItemRequest;
import com.fooddelivery.dto.request.RestaurantRequest;
import com.fooddelivery.dto.response.MenuItemResponse;
import com.fooddelivery.dto.response.RestaurantResponse;
import com.fooddelivery.enums.CuisineType;
import com.fooddelivery.service.MenuService;
import com.fooddelivery.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Restaurants", description = "Browse and manage restaurants & menus")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final MenuService       menuService;

    // ===== Public Endpoints =====

    @GetMapping
    @Operation(summary = "Browse restaurants with optional filters")
    public ResponseEntity<Page<RestaurantResponse>> getAllRestaurants(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CuisineType cuisine,
            @PageableDefault(size = 12, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(restaurantService.getAllRestaurants(keyword, cuisine, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get restaurant detail by ID")
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }

    @GetMapping("/{id}/menu")
    @Operation(summary = "Get available menu items for a restaurant")
    public ResponseEntity<List<MenuItemResponse>> getMenu(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getMenuByRestaurantId(id));
    }

    // ===== Restaurant Owner Endpoints =====

    @PostMapping
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Create a new restaurant")
    public ResponseEntity<RestaurantResponse> createRestaurant(
            @Valid @RequestBody RestaurantRequest dto,
            @AuthenticationPrincipal UserDetails principal) {
        RestaurantResponse response = restaurantService.createRestaurant(dto, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Update restaurant details")
    public ResponseEntity<RestaurantResponse> updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequest dto,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(restaurantService.updateRestaurant(id, dto, principal.getUsername()));
    }

    @PatchMapping("/{id}/toggle-open")
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Toggle restaurant open/closed status")
    public ResponseEntity<RestaurantResponse> toggleOpen(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(restaurantService.toggleRestaurantOpen(id, principal.getUsername()));
    }

    // ===== Menu Management =====

    @PostMapping("/{id}/menu")
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Add a menu item to a restaurant")
    public ResponseEntity<MenuItemResponse> addMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest dto,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(menuService.addMenuItem(id, dto, principal.getUsername()));
    }

    @PutMapping("/{id}/menu/{itemId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Update a menu item")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @Valid @RequestBody MenuItemRequest dto,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(menuService.updateMenuItem(itemId, dto, principal.getUsername()));
    }

    @DeleteMapping("/{id}/menu/{itemId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Delete a menu item")
    public ResponseEntity<Void> deleteMenuItem(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails principal) {
        menuService.deleteMenuItem(itemId, principal.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/menu/{itemId}/toggle-availability")
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Toggle menu item availability")
    public ResponseEntity<MenuItemResponse> toggleAvailability(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(menuService.toggleItemAvailability(itemId, principal.getUsername()));
    }
}
