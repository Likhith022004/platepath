package com.fooddelivery.repository;

import com.fooddelivery.entity.Restaurant;
import com.fooddelivery.enums.CuisineType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByOwner_Id(Long ownerId);

    List<Restaurant> findByCuisineType(CuisineType cuisineType);

    List<Restaurant> findByIsOpenTrue();

    @Query("SELECT r FROM Restaurant r WHERE " +
           "(:cuisine IS NULL OR r.cuisineType = :cuisine) AND " +
           "(:keyword IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(r.address) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Restaurant> searchRestaurants(
            @Param("cuisine") CuisineType cuisine,
            @Param("keyword") String keyword,
            Pageable pageable);
}
