package com.fooddelivery.repository;

import com.fooddelivery.entity.Order;
import com.fooddelivery.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomer_IdOrderByPlacedAtDesc(Long customerId);

    Page<Order> findByCustomer_Id(Long customerId, Pageable pageable);

    Page<Order> findByRestaurant_Id(Long restaurantId, Pageable pageable);

    List<Order> findByStatus(OrderStatus status);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    List<Order> findByDeliveryBoy_Id(Long deliveryBoyId);

    List<Order> findByDeliveryBoy_IdAndStatusIn(Long deliveryBoyId, List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.restaurant.id = :restaurantId " +
           "AND o.placedAt BETWEEN :startDate AND :endDate")
    List<Order> findByRestaurantAndDateRange(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM Order o WHERE o.deliveryBoy.id = :deliveryBoyId " +
           "AND o.status IN ('CONFIRMED', 'PREPARING', 'OUT_FOR_DELIVERY')")
    List<Order> findPendingOrdersForDeliveryBoy(@Param("deliveryBoyId") Long deliveryBoyId);

    @Query("SELECT o FROM Order o WHERE o.status IN :statuses " +
           "AND o.estimatedDeliveryTime < :threshold")
    List<Order> findDelayedOrders(
            @Param("statuses") List<OrderStatus> statuses,
            @Param("threshold") LocalDateTime threshold);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.placedAt >= :since")
    long countOrdersSince(@Param("since") LocalDateTime since);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'DELIVERED' " +
           "AND o.placedAt >= :since")
    BigDecimal sumRevenueDeliveredSince(@Param("since") LocalDateTime since);
}
