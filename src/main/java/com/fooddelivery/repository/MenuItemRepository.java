package com.fooddelivery.repository;

import com.fooddelivery.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByRestaurant_Id(Long restaurantId);

    List<MenuItem> findByRestaurant_IdAndIsAvailableTrue(Long restaurantId);

    List<MenuItem> findByRestaurant_IdAndCategory(Long restaurantId, String category);
}
